package com.example.basgaane.ui.playlists

/**
 * Utility functions for playlists presentation.
 */

/**
 * Formats track count into user-friendly string.
 */
fun formatPlaylistTrackCount(count: Int): String {
    return if (count == 1) "1 track" else "$count tracks"
}
