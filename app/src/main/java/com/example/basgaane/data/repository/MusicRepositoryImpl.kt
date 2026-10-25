package com.example.basgaane.data.repository

import android.content.Context
import android.net.Uri
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
    context: Context,
    private val localDataSource: LocalSongDataSource = LocalSongDataSource(context),
    private val remoteDataSource: FirebaseSongDataSource = FirebaseSongDataSource(),
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

        // 1. Add cloud tracks (synced across friends)
        for (song in cachedRemoteSongs) {
            combinedMap[song.id] = song
        }

        // 2. Add local tracks
        for (song in localList) {
            if (!combinedMap.containsKey(song.id)) {
                combinedMap[song.id] = song
            }
        }

        // 3. Populate default sample tracks if catalog is empty
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
        coverUrl: String?
    ): Result<Song> = withContext(Dispatchers.IO) {
        val songId = UUID.randomUUID().toString()

        // Try uploading to Firebase first
        val uploadResult = remoteDataSource.uploadAudio(audioUri, songId)
        if (uploadResult.isSuccess) {
            val audioUrl = uploadResult.getOrThrow()
            val cloudSong = Song(
                id = songId,
                title = title,
                artist = artist,
                audioUrl = audioUrl,
                coverUrl = coverUrl,
                durationMs = durationMs,
                isLocal = false,
                addedAt = System.currentTimeMillis()
            )
            remoteDataSource.saveSongDocument(cloudSong)
            localDataSource.saveSong(cloudSong)
            combineAndEmit()
            Result.success(cloudSong)
        } else {
            // Fall back to local storage if offline or rules block upload
            val localSong = Song(
                id = songId,
                title = title,
                artist = artist,
                audioUrl = audioUri.toString(),
                coverUrl = coverUrl,
                durationMs = durationMs,
                isLocal = true,
                addedAt = System.currentTimeMillis()
            )
            localDataSource.saveSong(localSong)
            combineAndEmit()
            Result.success(localSong)
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
}
