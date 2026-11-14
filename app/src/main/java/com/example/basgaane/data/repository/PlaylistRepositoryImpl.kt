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

        // 1. Process local playlists and union merge with remote counterparts
        for (localPl in localList) {
            val remotePl = cachedRemotePlaylists.find { it.id == localPl.id }
            if (remotePl != null) {
                val mergedSongIds = (localPl.songIds + remotePl.songIds).distinct()
                val mergedPl = localPl.copy(
                    songIds = mergedSongIds,
                    name = if (localPl.name.isNotBlank()) localPl.name else remotePl.name,
                    description = if (localPl.description.isNotBlank()) localPl.description else remotePl.description,
                    coverUrl = localPl.coverUrl ?: remotePl.coverUrl
                )
                // If local storage was missing items (e.g. fresh reinstall), save merged list to local prefs
                if (mergedSongIds.size > localPl.songIds.size) {
                    localDataSource.savePlaylist(mergedPl)
                }
                // If remote cloud was missing items, upload merged list to Firestore
                if (mergedSongIds.size > remotePl.songIds.size) {
                    coroutineScope.launch {
                        remoteDataSource.savePlaylistDocument(mergedPl)
                    }
                }
                combinedMap[mergedPl.id] = mergedPl
            } else {
                combinedMap[localPl.id] = localPl
                if (localPl.id == LocalPlaylistDataSource.LIKED_SONGS_PLAYLIST_ID && localPl.songIds.isNotEmpty()) {
                    coroutineScope.launch {
                        remoteDataSource.savePlaylistDocument(localPl)
                    }
                }
            }
        }

        // 2. Add any cloud playlists not yet stored locally
        for (remotePl in cachedRemotePlaylists) {
            if (!combinedMap.containsKey(remotePl.id)) {
                combinedMap[remotePl.id] = remotePl
                localDataSource.savePlaylist(remotePl)
            }
        }

        // 3. Ensure Liked Songs playlist is always pinned at the top
        val sortedList = combinedMap.values.sortedByDescending { it.createdAt }.toMutableList()
        val likedIndex = sortedList.indexOfFirst {
            it.id == LocalPlaylistDataSource.LIKED_SONGS_PLAYLIST_ID || it.name.equals("Liked Songs", ignoreCase = true)
        }
        if (likedIndex > 0) {
            val liked = sortedList.removeAt(likedIndex)
            sortedList.add(0, liked)
        }

        _playlistsFlow.value = sortedList
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
            cachedRemotePlaylists = cachedRemotePlaylists.filter { it.id != playlistId }
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
                cachedRemotePlaylists = cachedRemotePlaylists.map {
                    if (it.id == playlistId) updated else it
                }
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
                cachedRemotePlaylists = cachedRemotePlaylists.map {
                    if (it.id == playlistId) updated else it
                }
                remoteDataSource.savePlaylistDocument(updated)
            }
            combineAndEmit()
            Result.success(Unit)
        }

    companion object {
        private const val TAG = "PlaylistRepositoryImpl"
    }
}
