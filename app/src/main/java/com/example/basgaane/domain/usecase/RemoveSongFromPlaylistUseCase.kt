package com.example.basgaane.domain.usecase

import com.example.basgaane.domain.repository.PlaylistRepository

/**
 * Use case to remove a song from a playlist without deleting it from the app library.
 */
class RemoveSongFromPlaylistUseCase(
    private val repository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: String, songId: String): Result<Unit> {
        return repository.removeSongFromPlaylist(playlistId, songId)
    }
}
