package com.example.basgaane.data.datasource.local

import android.content.Context
import android.content.SharedPreferences
import com.example.basgaane.domain.model.Song
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local data source managing on-device persistent JSON storage and starter demo tracks.
 */
class LocalSongDataSource(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("basgaane_local_store", Context.MODE_PRIVATE)

    fun getLocalSongs(): MutableList<Song> {
        val storedJson = prefs.getString(KEY_LOCAL_SONGS, null)
        val list = mutableListOf<Song>()

        if (!storedJson.isNullOrEmpty()) {
            try {
                val array = JSONArray(storedJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        Song(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            artist = obj.optString("artist", "Unknown Artist"),
                            audioUrl = obj.getString("audioUrl"),
                            coverUrl = obj.optString("coverUrl").takeIf { it.isNotEmpty() },
                            durationMs = obj.optLong("durationMs", 0L),
                            isLocal = obj.optBoolean("isLocal", true),
                            addedAt = obj.optLong("addedAt", System.currentTimeMillis()),
                            addedBy = obj.optString("addedBy", "Friend").takeIf { it.isNotBlank() } ?: "Friend"
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return list
    }

    fun saveSong(song: Song) {
        val current = getLocalSongs()
        current.removeAll { it.id == song.id }
        current.add(0, song)
        persist(current)
    }

    fun removeSong(songId: String) {
        val current = getLocalSongs()
        current.removeAll { it.id == songId }
        persist(current)
    }

    private fun persist(songs: List<Song>) {
        val array = JSONArray()
        for (song in songs) {
            val obj = JSONObject().apply {
                put("id", song.id)
                put("title", song.title)
                put("artist", song.artist)
                put("audioUrl", song.audioUrl)
                put("coverUrl", song.coverUrl ?: "")
                put("durationMs", song.durationMs)
                put("isLocal", song.isLocal)
                put("addedAt", song.addedAt)
                put("addedBy", song.addedBy)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_LOCAL_SONGS, array.toString()).apply()
    }

    companion object {
        private const val KEY_LOCAL_SONGS = "key_local_songs_json"

        /**
         * Curated royalty-free starter tracks to give users an immediate playback experience.
         */
        val sampleStarterSongs = listOf(
            Song(
                id = "sample-1",
                title = "Starry Neon Night",
                artist = "SoundHelix Synth",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                coverUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&auto=format&fit=crop&q=80",
                durationMs = 372000L,
                isLocal = false,
                addedBy = "BasGaane"
            ),
            Song(
                id = "sample-2",
                title = "Midnight Groove",
                artist = "Cyber Pulse",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                coverUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop&q=80",
                durationMs = 423000L,
                isLocal = false,
                addedBy = "BasGaane"
            ),
            Song(
                id = "sample-3",
                title = "Acoustic Horizon",
                artist = "Echo Dream",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                coverUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop&q=80",
                durationMs = 340000L,
                isLocal = false,
                addedBy = "BasGaane"
            ),
            Song(
                id = "sample-4",
                title = "Cosmic Chillout",
                artist = "Lofi Odyssey",
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                coverUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=500&auto=format&fit=crop&q=80",
                durationMs = 302000L,
                isLocal = false,
                addedBy = "BasGaane"
            )
        )
    }
}
