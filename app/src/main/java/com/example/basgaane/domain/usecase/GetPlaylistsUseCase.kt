package com.example.basgaane.domain.usecase

import com.example.basgaane.domain.model.Playlist
import com.example.basgaane.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe all playlists (both local and cloud-synced).
 */
class GetPlaylistsUseCase(
    private val repository: PlaylistRepository
) {
    operator fun invoke(): Flow<List<Playlist>> = repository.getPlaylists()
}
