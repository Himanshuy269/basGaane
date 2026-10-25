package com.example.basgaane.domain.usecase

import com.example.basgaane.domain.repository.PlaylistRepository

/**
 * Use case to delete a playlist.
 */
class DeletePlaylistUseCase(
    private val repository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: String): Result<Unit> {
        return repository.deletePlaylist(playlistId)
    }
}
