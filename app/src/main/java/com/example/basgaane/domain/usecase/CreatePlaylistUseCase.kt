package com.example.basgaane.domain.usecase

import com.example.basgaane.domain.model.Playlist
import com.example.basgaane.domain.repository.PlaylistRepository

/**
 * Use case to create a new custom playlist.
 */
class CreatePlaylistUseCase(
    private val repository: PlaylistRepository
) {
    suspend operator fun invoke(name: String, description: String = ""): Result<Playlist> {
        return repository.createPlaylist(name, description)
    }
}
