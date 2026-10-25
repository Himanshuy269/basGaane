package com.example.basgaane.data.datasource.local

import android.content.Context
import android.content.SharedPreferences
import com.example.basgaane.domain.model.Playlist
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local data source managing on-device persistent JSON storage of playlists.
 */
class LocalPlaylistDataSource(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("basgaane_playlist_store", Context.MODE_PRIVATE)

    fun getLocalPlaylists(): MutableList<Playlist> {
        val storedJson = prefs.getString(KEY_PLAYLISTS, null)
        val list = mutableListOf<Playlist>()

        if (!storedJson.isNullOrEmpty()) {
            try {
                val array = JSONArray(storedJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val songIdsArray = obj.optJSONArray("songIds") ?: JSONArray()
                    val songIds = mutableListOf<String>()
                    for (j in 0 until songIdsArray.length()) {
                        songIds.add(songIdsArray.getString(j))
                    }

                    list.add(
                        Playlist(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            description = obj.optString("description", ""),
                            songIds = songIds,
                            coverUrl = obj.optString("coverUrl").takeIf { it.isNotEmpty() },
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Initialize with default Favorites playlist if empty
        if (list.isEmpty() && !prefs.getBoolean(KEY_INITIALIZED, false)) {
            list.addAll(defaultPlaylists)
            persist(list)
            prefs.edit().putBoolean(KEY_INITIALIZED, true).apply()
        }

        return list
    }

    fun savePlaylist(playlist: Playlist) {
        val current = getLocalPlaylists()
        current.removeAll { it.id == playlist.id }
        current.add(0, playlist)
        persist(current)
    }

    fun deletePlaylist(playlistId: String) {
        val current = getLocalPlaylists()
        current.removeAll { it.id == playlistId }
        persist(current)
    }

    fun addSongToPlaylist(playlistId: String, songId: String) {
        val current = getLocalPlaylists()
        val index = current.indexOfFirst { it.id == playlistId }
        if (index != -1) {
            val playlist = current[index]
            if (!playlist.songIds.contains(songId)) {
                val updatedIds = playlist.songIds + songId
                current[index] = playlist.copy(songIds = updatedIds)
                persist(current)
            }
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        val current = getLocalPlaylists()
        val index = current.indexOfFirst { it.id == playlistId }
        if (index != -1) {
            val playlist = current[index]
            val updatedIds = playlist.songIds.filter { it != songId }
            current[index] = playlist.copy(songIds = updatedIds)
            persist(current)
        }
    }

    private fun persist(playlists: List<Playlist>) {
        val array = JSONArray()
        for (pl in playlists) {
            val obj = JSONObject().apply {
                put("id", pl.id)
                put("name", pl.name)
                put("description", pl.description)
                val songIdsArray = JSONArray()
                for (id in pl.songIds) {
                    songIdsArray.put(id)
                }
                put("songIds", songIdsArray)
                put("coverUrl", pl.coverUrl ?: "")
                put("createdAt", pl.createdAt)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_PLAYLISTS, array.toString()).apply()
    }

    companion object {
        private const val KEY_PLAYLISTS = "key_playlists_json"
        private const val KEY_INITIALIZED = "key_playlists_initialized"

        val defaultPlaylists = listOf(
            Playlist(
                id = "fav-playlist-1",
                name = "Favorites",
                description = "My favorite tracks & vibes",
                songIds = listOf("sample-1", "sample-2")
            ),
            Playlist(
                id = "chill-playlist-2",
                name = "Chill & Lo-Fi",
                description = "Relaxing late night listening",
                songIds = listOf("sample-3", "sample-4")
            )
        )
    }
}
