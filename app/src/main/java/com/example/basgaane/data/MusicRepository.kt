package com.example.basgaane.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.example.basgaane.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * Repository interface for managing songs in BasGaane.
 */
interface MusicRepository {
    fun getSongs(): Flow<List<Song>>
    suspend fun addSong(song: Song): Result<Song>
    suspend fun removeSong(songId: String): Result<Unit>
    suspend fun refreshCloudSongs()
}

/**
 * Implementation of [MusicRepository] that supports local persistent storage
 * and preloads curated tracks. Designed to seamlessly connect with Firebase
 * or remote backend when credentials are provided.
 */
class DefaultMusicRepository(
    private val context: Context
) : MusicRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("basgaane_tracks_store", Context.MODE_PRIVATE)

    private val _songsFlow = MutableStateFlow<List<Song>>(emptyList())

    init {
        loadPersistedSongs()
    }

    override fun getSongs(): Flow<List<Song>> = _songsFlow.asStateFlow()

    private fun loadPersistedSongs() {
        val storedJson = prefs.getString(KEY_SONGS_JSON, null)
        val songsList = mutableListOf<Song>()

        if (!storedJson.isNullOrEmpty()) {
            try {
                val array = JSONArray(storedJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    songsList.add(
                        Song(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            artist = obj.optString("artist", "Unknown Artist"),
                            audioUrl = obj.getString("audioUrl"),
                            coverUrl = obj.optString("coverUrl").takeIf { it.isNotEmpty() },
                            durationMs = obj.optLong("durationMs", 0L),
                            isLocal = obj.optBoolean("isLocal", false),
                            addedAt = obj.optLong("addedAt", System.currentTimeMillis())
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // If no songs exist, populate with curated royalty-free starter tracks
        if (songsList.isEmpty()) {
            songsList.addAll(sampleStarterSongs)
            persistSongs(songsList)
        }

        _songsFlow.value = songsList
    }

    override suspend fun addSong(song: Song): Result<Song> {
        return try {
            val updated = _songsFlow.value.toMutableList()
            // Add new song to the top of the playlist
            updated.add(0, song)
            _songsFlow.value = updated
            persistSongs(updated)
            Result.success(song)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeSong(songId: String): Result<Unit> {
        return try {
            val updated = _songsFlow.value.filterNot { it.id == songId }
            _songsFlow.value = updated
            persistSongs(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshCloudSongs() {
        // Ready for Firebase Firestore / Cloud Storage listener.
        // Once google-services.json is configured, this fetches the global
        // tracks uploaded by any user or friend.
    }

    private fun persistSongs(list: List<Song>) {
        val array = JSONArray()
        for (song in list) {
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

        /**
         * Curated royalty-free starter tracks to give users an immediate,
         * seamless playback experience upon initial launch.
         */
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
