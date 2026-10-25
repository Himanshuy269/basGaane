package com.example.basgaane.ui.home

import java.util.Locale

/**
 * Screen-specific utility and helper functions for HomeScreen.
 */
fun formatTrackDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}
