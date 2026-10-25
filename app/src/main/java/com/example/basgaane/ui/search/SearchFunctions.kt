package com.example.basgaane.ui.search

import com.example.basgaane.domain.model.Song
import java.util.Locale

/**
 * Utility functions for the search screen.
 */

/**
 * Formats duration in milliseconds to mm:ss format.
 */
fun formatSearchDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}

/**
 * Evaluates whether a track matches the selected category or source tag.
 */
fun matchesFilterTag(song: Song, tag: String): Boolean {
    return when (tag.lowercase(Locale.getDefault())) {
        "all" -> true
        "cloud" -> !song.isLocal
        "device" -> song.isLocal
        "synth", "groove", "chill", "acoustic" ->
            song.title.contains(tag, ignoreCase = true) || song.artist.contains(tag, ignoreCase = true)
        else -> true
    }
}
