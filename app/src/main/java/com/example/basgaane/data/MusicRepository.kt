package com.example.basgaane.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.example.basgaane.model.Song
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Repository interface for managing songs in BasGaane.
 */
interface MusicRepository {
    fun getSongs(): Flow<List<Song>>
    suspend fun addSong(song: Song): Result<Song>
    suspend fun uploadSongToCloud(
        title: String,
        artist: String,
        audioUri: Uri,
        durationMs: Long,
        coverUrl: String? = null
    ): Result<Song>
    suspend fun removeSong(songId: String): Result<Unit>
    fun refreshCloudSongs()
}

/**
 * Implementation of [MusicRepository] that unifies:
 * 1. Firebase Cloud Storage (audio streaming) & Firestore (multi-device real-time sync for friends)
 * 2. Local persistent caching (SharedPreferences JSON)
 * 3. Royalty-free starter tracks
 */
class DefaultMusicRepository(
    private val context: Context
) : MusicRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("basgaane_tracks_store", Context.MODE_PRIVATE)

    private val _songsFlow = MutableStateFlow<List<Song>>(emptyList())
    private var firestoreListener: ListenerRegistration? = null

    private var localSongs: MutableList<Song> = mutableListOf()
    private var cloudSongs: MutableList<Song> = mutableListOf()

    init {
        loadPersistedLocalSongs()
        initFirebaseRealtimeSync()
    }

    override fun getSongs(): Flow<List<Song>> = _songsFlow.asStateFlow()

    private fun loadPersistedLocalSongs() {
        val storedJson = prefs.getString(KEY_SONGS_JSON, null)
        val loadedList = mutableListOf<Song>()

        if (!storedJson.isNullOrEmpty()) {
            try {
                val array = JSONArray(storedJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    loadedList.add(
                        Song(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            artist = obj.optString("artist", "Unknown Artist"),
                            audioUrl = obj.getString("audioUrl"),
                            coverUrl = obj.optString("coverUrl").takeIf { it.isNotEmpty() },
                            durationMs = obj.optLong("durationMs", 0L),
                            isLocal = obj.optBoolean("isLocal", true),
                            addedAt = obj.optLong("addedAt", System.currentTimeMillis())
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        localSongs = loadedList
        combineAndEmitSongs()
    }

    private fun initFirebaseRealtimeSync() {
        try {
            val firestore = FirebaseFirestore.getInstance()
            firestoreListener?.remove()
            firestoreListener = firestore.collection(FIRESTORE_COLLECTION_SONGS)
                .orderBy("addedAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }

                    if (snapshots != null) {
                        val remoteList = mutableListOf<Song>()
                        for (doc in snapshots.documents) {
                            val id = doc.getString("id") ?: doc.id
                            val title = doc.getString("title") ?: continue
                            val artist = doc.getString("artist") ?: "Unknown Artist"
                            val audioUrl = doc.getString("audioUrl") ?: continue
                            val coverUrl = doc.getString("coverUrl")
                            val durationMs = doc.getLong("durationMs") ?: 0L
                            val addedAt = doc.getLong("addedAt") ?: System.currentTimeMillis()

                            remoteList.add(
                                Song(
                                    id = id,
                                    title = title,
                                    artist = artist,
                                    audioUrl = audioUrl,
                                    coverUrl = coverUrl,
                                    durationMs = durationMs,
                                    isLocal = false,
                                    addedAt = addedAt
                                )
                            )
                        }
                        cloudSongs = remoteList
                        combineAndEmitSongs()
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun refreshCloudSongs() {
        initFirebaseRealtimeSync()
    }

    private fun combineAndEmitSongs() {
        val combinedMap = linkedMapOf<String, Song>()

        // 1. Cloud shared songs (synced across all friends)
        for (song in cloudSongs) {
            combinedMap[song.id] = song
        }

        // 2. Locally added songs
        for (song in localSongs) {
            if (!combinedMap.containsKey(song.id)) {
                combinedMap[song.id] = song
            }
        }

        // 3. Fallback starter tracks if user has zero songs
        if (combinedMap.isEmpty()) {
            for (sample in sampleStarterSongs) {
                combinedMap[sample.id] = sample
            }
        }

        val sortedList = combinedMap.values.sortedByDescending { it.addedAt }
        _songsFlow.value = sortedList
    }

    override suspend fun uploadSongToCloud(
        title: String,
        artist: String,
        audioUri: Uri,
        durationMs: Long,
        coverUrl: String?
    ): Result<Song> = withContext(Dispatchers.IO) {
        val songId = UUID.randomUUID().toString()
        try {
            // 1. Upload audio file to Firebase Cloud Storage
            val storageRef = FirebaseStorage.getInstance().reference
            val audioFileName = "audio_${songId}.mp3"
            val fileRef = storageRef.child("audio").child(audioFileName)

            Tasks.await(fileRef.putFile(audioUri))
            val downloadUrl = Tasks.await(fileRef.downloadUrl).toString()

            // 2. Create Song model
            val cloudSong = Song(
                id = songId,
                title = title,
                artist = artist,
                audioUrl = downloadUrl,
                coverUrl = coverUrl,
                durationMs = durationMs,
                isLocal = false,
                addedAt = System.currentTimeMillis()
            )

            // 3. Save track metadata to Cloud Firestore for friends to access
            val firestore = FirebaseFirestore.getInstance()
            val songData = hashMapOf(
                "id" to cloudSong.id,
                "title" to cloudSong.title,
                "artist" to cloudSong.artist,
                "audioUrl" to cloudSong.audioUrl,
                "coverUrl" to (cloudSong.coverUrl ?: ""),
                "durationMs" to cloudSong.durationMs,
                "isLocal" to false,
                "addedAt" to cloudSong.addedAt
            )
            Tasks.await(firestore.collection(FIRESTORE_COLLECTION_SONGS).document(songId).set(songData))

            // Keep local reference as well
            localSongs.add(0, cloudSong)
            persistLocalSongs()
            combineAndEmitSongs()

            Result.success(cloudSong)
        } catch (e: Exception) {
            // If cloud upload fails (e.g., offline or storage rules), fall back to local save
            val fallbackLocalSong = Song(
                id = songId,
                title = title,
                artist = artist,
                audioUrl = audioUri.toString(),
                coverUrl = coverUrl,
                durationMs = durationMs,
                isLocal = true,
                addedAt = System.currentTimeMillis()
            )
            localSongs.add(0, fallbackLocalSong)
            persistLocalSongs()
            combineAndEmitSongs()

            Result.success(fallbackLocalSong)
        }
    }

    override suspend fun addSong(song: Song): Result<Song> = withContext(Dispatchers.IO) {
        try {
            localSongs.add(0, song)
            persistLocalSongs()
            combineAndEmitSongs()
            Result.success(song)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeSong(songId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            localSongs.removeAll { it.id == songId }
            cloudSongs.removeAll { it.id == songId }
            persistLocalSongs()
            combineAndEmitSongs()

            // Try removing from Firestore if it was a cloud song
            try {
                FirebaseFirestore.getInstance()
                    .collection(FIRESTORE_COLLECTION_SONGS)
                    .document(songId)
                    .delete()
            } catch (e: Exception) {
                // Ignore remote deletion errors
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun persistLocalSongs() {
        val array = JSONArray()
        for (song in localSongs) {
            val obj = JSONObject().apply {
                put("id", song.id)
                put("title", song.title)
                put("artist", song.artist)
                put("audioUrl", song.audioUrl)
                put("coverUrl", song.coverUrl ?: "")
                put("durationMs", song.durationMs)
                put("isLocal", song.isLocal)
                put("addedAt", song.addedAt)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_SONGS_JSON, array.toString()).apply()
    }

    companion object {
        private const val KEY_SONGS_JSON = "key_songs_json"
        private const val FIRESTORE_COLLECTION_SONGS = "songs"

        val sampleStarterSongs = listOf(
            Song(
                id = "sample-1",
                title = "Starry Neon Night",
                artist = "SoundHelix Synth",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&auto=format&fit=crop&q=80",
                durationMs = 372000L,
                isLocal = false
            ),
            Song(
                id = "sample-2",
                title = "Midnight Groove",
                artist = "Cyber Pulse",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop&q=80",
                durationMs = 423000L,
                isLocal = false
            ),
            Song(
                id = "sample-3",
                title = "Acoustic Horizon",
                artist = "Echo Dream",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop&q=80",
                durationMs = 340000L,
                isLocal = false
            ),
            Song(
                id = "sample-4",
                title = "Cosmic Chillout",
                artist = "Lofi Odyssey",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                coverUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=500&auto=format&fit=crop&q=80",
                durationMs = 302000L,
                isLocal = false
            )
        )
    }
}
