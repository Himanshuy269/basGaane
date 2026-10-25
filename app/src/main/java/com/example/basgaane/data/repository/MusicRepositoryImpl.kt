package com.example.basgaane.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.basgaane.core.utils.AudioMetadataHelper
import com.example.basgaane.data.datasource.local.LocalSongDataSource
import com.example.basgaane.data.datasource.remote.FirebaseSongDataSource
import com.example.basgaane.domain.model.Song
import com.example.basgaane.domain.repository.MusicRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Implementation of [MusicRepository] unifying local persistent storage
 * with Firebase Cloud Storage and Firestore real-time sync.
 */
class MusicRepositoryImpl(
    private val context: Context,
    private val localDataSource: LocalSongDataSource = LocalSongDataSource(context),
    private val remoteDataSource: FirebaseSongDataSource = FirebaseSongDataSource(context),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : MusicRepository {

    private val _songsFlow = MutableStateFlow<List<Song>>(emptyList())
    private var cachedRemoteSongs: List<Song> = emptyList()

    init {
        // 1. Load local tracks initially
        combineAndEmit()

        // 2. Observe remote Firestore updates in real time
        coroutineScope.launch {
            remoteDataSource.observeRemoteSongs().collect { remoteList ->
                Log.d(TAG, "Syncing remote songs: ${remoteList.size} tracks found on cloud.")
                cachedRemoteSongs = remoteList
                combineAndEmit()
            }
        }
    }

    override fun getSongs(): Flow<List<Song>> = _songsFlow.asStateFlow()

    override fun refreshSongs() {
        combineAndEmit()
    }

    private fun combineAndEmit() {
        val localList = localDataSource.getLocalSongs()
        val combinedMap = linkedMapOf<String, Song>()

        // 1. Add cloud tracks (synced in real time across friends' devices)
        for (song in cachedRemoteSongs) {
            combinedMap[song.id] = song
        }

        // 2. Add local tracks (backfilling embedded album art if missing)
        for (song in localList) {
            if (!combinedMap.containsKey(song.id)) {
                val songWithCover = if (song.coverUrl.isNullOrEmpty() && (song.audioUrl.startsWith("content://") || song.audioUrl.startsWith("file://"))) {
                    val pic = AudioMetadataHelper.extractEmbeddedPicture(context, song.audioUrl)
                    if (pic != null) {
                        val localPath = AudioMetadataHelper.saveCoverLocally(context, song.id, pic)
                        if (localPath != null) {
                            val updated = song.copy(coverUrl = localPath)
                            localDataSource.saveSong(updated)
                            updated
                        } else song
                    } else song
                } else song
                combinedMap[song.id] = songWithCover
            }
        }

        // 3. Fallback demo tracks if user has zero songs anywhere
        if (combinedMap.isEmpty()) {
            for (sample in LocalSongDataSource.sampleStarterSongs) {
                combinedMap[sample.id] = sample
            }
        }

        _songsFlow.value = combinedMap.values.sortedByDescending { it.addedAt }
    }

    override suspend fun uploadAndAddSong(
        title: String,
        artist: String,
        audioUri: Uri,
        durationMs: Long,
        coverBytes: ByteArray?,
        coverUri: Uri?,
        coverUrl: String?,
        addedBy: String
    ): Result<Song> = withContext(Dispatchers.IO) {
        val songId = UUID.randomUUID().toString()

        Log.d(TAG, "Starting uploadAndAddSong for '$title' ($songId) added by '$addedBy'...")

        // 1. Resolve cover image bytes (from parameter, image Uri, or embedded ID3 picture in audioUri)
        var resolvedCoverBytes: ByteArray? = coverBytes
        if (resolvedCoverBytes == null && coverUri != null) {
            try {
                resolvedCoverBytes = context.contentResolver.openInputStream(coverUri)?.use { it.readBytes() }
            } catch (e: Exception) {
                Log.w(TAG, "Could not read coverUri: ${e.message}")
            }
        }
        if (resolvedCoverBytes == null && coverUrl.isNullOrEmpty()) {
            resolvedCoverBytes = AudioMetadataHelper.extractEmbeddedPicture(context, audioUri.toString())
        }

        // Cache cover locally if bytes are present
        var localCoverUrl: String? = null
        if (resolvedCoverBytes != null) {
            localCoverUrl = AudioMetadataHelper.saveCoverLocally(context, songId, resolvedCoverBytes)
        }

        // 2. Upload audio file to Cloud Storage
        val uploadResult = remoteDataSource.uploadAudio(audioUri, songId)

        if (uploadResult.isSuccess) {
            val audioUrl = uploadResult.getOrThrow()

            // 3. Upload cover image to Cloud Storage if available so all friends see it
            var finalRemoteCoverUrl: String? = coverUrl
            if (resolvedCoverBytes != null) {
                val coverUploadResult = remoteDataSource.uploadCoverImage(resolvedCoverBytes, songId)
                if (coverUploadResult.isSuccess) {
                    finalRemoteCoverUrl = coverUploadResult.getOrThrow()
                } else {
                    Log.w(TAG, "Audio uploaded, but cover image upload failed: ${coverUploadResult.exceptionOrNull()?.message}")
                    finalRemoteCoverUrl = localCoverUrl ?: coverUrl
                }
            } else if (finalRemoteCoverUrl.isNullOrEmpty()) {
                finalRemoteCoverUrl = localCoverUrl
            }

            val cloudSong = Song(
                id = songId,
                title = title,
                artist = artist,
                audioUrl = audioUrl,
                coverUrl = finalRemoteCoverUrl,
                durationMs = durationMs,
                isLocal = false,
                addedAt = System.currentTimeMillis(),
                addedBy = addedBy
            )

            // Save to Firestore so friends receive it via real-time snapshot
            val firestoreResult = remoteDataSource.saveSongDocument(cloudSong)
            if (firestoreResult.isSuccess) {
                // Also cache locally with the cloud download URL
                localDataSource.saveSong(cloudSong)
                combineAndEmit()
                Log.d(TAG, "Song successfully uploaded and published to Firestore with coverUrl: $finalRemoteCoverUrl")
                Result.success(cloudSong)
            } else {
                val err = firestoreResult.exceptionOrNull()
                Log.e(TAG, "Audio uploaded but Firestore metadata write failed: ${err?.message}")
                localDataSource.saveSong(cloudSong)
                combineAndEmit()
                Result.failure(Exception("Audio uploaded, but metadata write failed: ${err?.localizedMessage}. Check Firestore security rules!"))
            }
        } else {
            val err = uploadResult.exceptionOrNull()
            Log.e(TAG, "Cloud storage upload failed: ${err?.message}", err)

            // Save locally for offline listening on this phone
            val localSong = Song(
                id = songId,
                title = title,
                artist = artist,
                audioUrl = audioUri.toString(),
                coverUrl = localCoverUrl ?: coverUrl,
                durationMs = durationMs,
                isLocal = true,
                addedAt = System.currentTimeMillis(),
                addedBy = addedBy
            )
            localDataSource.saveSong(localSong)
            combineAndEmit()

            // Return failure so the user knows it could not be synced to friends
            Result.failure(Exception("Saved on this phone, but cloud sync failed: ${err?.localizedMessage ?: "Check Firebase Storage rules"}"))
        }
    }

    override suspend fun addSong(song: Song): Result<Song> = withContext(Dispatchers.IO) {
        localDataSource.saveSong(song)
        combineAndEmit()
        Result.success(song)
    }

    override suspend fun removeSong(songId: String): Result<Unit> = withContext(Dispatchers.IO) {
        localDataSource.removeSong(songId)
        remoteDataSource.deleteSongDocument(songId)
        combineAndEmit()
        Result.success(Unit)
    }

    companion object {
        private const val TAG = "MusicRepositoryImpl"
    }
}
