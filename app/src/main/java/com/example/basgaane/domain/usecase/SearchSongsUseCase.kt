package com.example.basgaane.domain.usecase

import com.example.basgaane.domain.model.Song

/**
 * Use case to filter tracks by search keyword (matching title or artist).
 */
class SearchSongsUseCase {
    operator fun invoke(songs: List<Song>, query: String): List<Song> {
        if (query.isBlank()) return songs
        val trimmedQuery = query.trim()
        return songs.filter { song ->
            song.title.contains(trimmedQuery, ignoreCase = true) ||
                    song.artist.contains(trimmedQuery, ignoreCase = true)
        }
    }
}
