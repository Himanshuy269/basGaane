package com.example.basgaane.ui.playlistdetail

import com.example.basgaane.domain.model.Song
import java.util.Locale

/**
 * Helper functions for playlist detail calculations and formatting.
 */

/**
 * Formats duration in milliseconds to mm:ss or hh:mm:ss.
 */
fun formatDetailDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}

/**
 * Computes the total song count and cumulative duration for a playlist.
 */
fun calculatePlaylistStats(songs: List<Song>): Pair<Int, Long> {
    val count = songs.size
    val totalMs = songs.sumOf { it.durationMs }
    return Pair(count, totalMs)
}
