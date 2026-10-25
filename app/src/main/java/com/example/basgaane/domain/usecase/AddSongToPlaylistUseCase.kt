package com.example.basgaane.domain.usecase

import com.example.basgaane.domain.repository.PlaylistRepository

/**
 * Use case to add a song to an existing playlist.
 */
class AddSongToPlaylistUseCase(
    private val repository: PlaylistRepository
) {
    suspend operator fun invoke(playlistId: String, songId: String): Result<Unit> {
        return repository.addSongToPlaylist(playlistId, songId)
    }
}
