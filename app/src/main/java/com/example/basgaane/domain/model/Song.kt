package com.example.basgaane.domain.model

import androidx.compose.runtime.Immutable
import java.util.UUID

/**
 * Domain entity representing a music track.
 * Marked @Immutable so Compose compiler skips item recompositions during scrolling.
 */
@Immutable
data class Song(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val artist: String = "Unknown Artist",
    val audioUrl: String,
    val coverUrl: String? = null,
    val durationMs: Long = 0L,
    val isLocal: Boolean = false,
    val addedAt: Long = System.currentTimeMillis(),
    val addedBy: String = "Friend"
)
