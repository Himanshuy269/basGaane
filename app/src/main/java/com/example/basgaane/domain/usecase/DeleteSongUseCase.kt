package com.example.basgaane.domain.usecase

import com.example.basgaane.domain.repository.MusicRepository

/**
 * Use case to delete a track from the library.
 */
class DeleteSongUseCase(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(songId: String): Result<Unit> {
        return repository.removeSong(songId)
    }
}
