package com.example.basgaane.domain.repository

import com.example.basgaane.domain.model.Playlist
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract defining playlist curation operations.
 */
interface PlaylistRepository {
    fun getPlaylists(): Flow<List<Playlist>>
    suspend fun createPlaylist(name: String, description: String = ""): Result<Playlist>
    suspend fun deletePlaylist(playlistId: String): Result<Unit>
    suspend fun addSongToPlaylist(playlistId: String, songId: String): Result<Unit>
    suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Result<Unit>
    fun refreshPlaylists()
}
