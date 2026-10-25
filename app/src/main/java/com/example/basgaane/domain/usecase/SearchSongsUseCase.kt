package com.example.basgaane.domain.usecase

import com.example.basgaane.domain.model.Song

/**
 * Use case to filter tracks by search keyword (matching title or artist)
 * and an optional category/source filter tag (e.g. "All", "Cloud", "Device").
 */
class SearchSongsUseCase {
    operator fun invoke(
        songs: List<Song>,
        query: String,
        filterTag: String = "All"
    ): List<Song> {
        var result = songs

        // Filter by category / source tag if not "All"
        result = when (filterTag.lowercase()) {
            "cloud" -> result.filter { !it.isLocal }
            "device" -> result.filter { it.isLocal }
            "all" -> result
            else -> result.filter { song ->
                song.title.contains(filterTag, ignoreCase = true) ||
                        song.artist.contains(filterTag, ignoreCase = true)
            }
        }

        // Filter by search query if present
        if (query.isNotBlank()) {
            val trimmed = query.trim()
            result = result.filter { song ->
                song.title.contains(trimmed, ignoreCase = true) ||
                        song.artist.contains(trimmed, ignoreCase = true)
            }
        }

        return result
    }
}
