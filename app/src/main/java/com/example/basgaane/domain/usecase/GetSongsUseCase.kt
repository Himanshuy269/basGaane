package com.example.basgaane.domain.usecase

import com.example.basgaane.domain.model.Song
import com.example.basgaane.domain.repository.MusicRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to observe the stream of all available music tracks.
 */
class GetSongsUseCase(
    private val repository: MusicRepository
) {
    operator fun invoke(): Flow<List<Song>> {
        return repository.getSongs()
    }
}
