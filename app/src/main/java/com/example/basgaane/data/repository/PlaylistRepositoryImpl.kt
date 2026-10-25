package com.example.basgaane.data.repository

import android.content.Context
import android.util.Log
import com.example.basgaane.data.datasource.local.LocalPlaylistDataSource
import com.example.basgaane.data.datasource.remote.FirebasePlaylistDataSource
import com.example.basgaane.domain.model.Playlist
import com.example.basgaane.domain.repository.PlaylistRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Implementation of [PlaylistRepository] unifying local persistent JSON storage
 * with Firestore real-time synchronization between friends.
 */
class PlaylistRepositoryImpl(
    context: Context,
    private val localDataSource: LocalPlaylistDataSource = LocalPlaylistDataSource(context),
    private val remoteDataSource: FirebasePlaylistDataSource = FirebasePlaylistDataSource(context),
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : PlaylistRepository {

    private val _playlistsFlow = MutableStateFlow<List<Playlist>>(emptyList())
    private var cachedRemotePlaylists: List<Playlist> = emptyList()

    init {
        combineAndEmit()

        coroutineScope.launch {
            remoteDataSource.observeRemotePlaylists().collect { remoteList ->
                Log.d(TAG, "Syncing remote playlists: ${remoteList.size} playlists found on cloud.")
                cachedRemotePlaylists = remoteList
                combineAndEmit()
            }
        }
    }

    override fun getPlaylists(): Flow<List<Playlist>> = _playlistsFlow.asStateFlow()

    override fun refreshPlaylists() {
        combineAndEmit()
    }

    private fun combineAndEmit() {
        val localList = localDataSource.getLocalPlaylists()
        val combinedMap = linkedMapOf<String, Playlist>()

        // 1. Cloud playlists
        for (pl in cachedRemotePlaylists) {
            combinedMap[pl.id] = pl
        }

        // 2. Local playlists
        for (pl in localList) {
            if (!combinedMap.containsKey(pl.id)) {
                combinedMap[pl.id] = pl
            }
        }

        _playlistsFlow.value = combinedMap.values.sortedByDescending { it.createdAt }
    }

    override suspend fun createPlaylist(name: String, description: String): Result<Playlist> =
        withContext(Dispatchers.IO) {
            val playlist = Playlist(
                id = UUID.randomUUID().toString(),
                name = name.trim().ifEmpty { "New Playlist" },
                description = description.trim(),
                songIds = emptyList(),
                createdAt = System.currentTimeMillis()
            )

            localDataSource.savePlaylist(playlist)
            combineAndEmit()

            // Also sync to Firestore so friends can see it
            remoteDataSource.savePlaylistDocument(playlist)

            Result.success(playlist)
        }

    override suspend fun deletePlaylist(playlistId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            localDataSource.deletePlaylist(playlistId)
            remoteDataSource.deletePlaylistDocument(playlistId)
            combineAndEmit()
            Result.success(Unit)
        }

    override suspend fun addSongToPlaylist(playlistId: String, songId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            localDataSource.addSongToPlaylist(playlistId, songId)
            val updated = localDataSource.getLocalPlaylists().find { it.id == playlistId }
                ?: cachedRemotePlaylists.find { it.id == playlistId }?.let {
                    if (!it.songIds.contains(songId)) it.copy(songIds = it.songIds + songId) else it
                }

            if (updated != null) {
                remoteDataSource.savePlaylistDocument(updated)
            }
            combineAndEmit()
            Result.success(Unit)
        }

    override suspend fun removeSongFromPlaylist(playlistId: String, songId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            localDataSource.removeSongFromPlaylist(playlistId, songId)
            val updated = localDataSource.getLocalPlaylists().find { it.id == playlistId }
                ?: cachedRemotePlaylists.find { it.id == playlistId }?.let {
                    it.copy(songIds = it.songIds.filter { id -> id != songId })
                }

            if (updated != null) {
                remoteDataSource.savePlaylistDocument(updated)
            }
            combineAndEmit()
            Result.success(Unit)
        }

    companion object {
        private const val TAG = "PlaylistRepositoryImpl"
    }
}
