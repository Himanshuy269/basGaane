package com.example.basgaane.domain.model

import java.util.UUID

/**
 * Domain entity representing a music track.
 * Pure business model without platform dependencies.
 */
data class Song(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val artist: String = "Unknown Artist",
    val audioUrl: String,
    val coverUrl: String? = null,
    val durationMs: Long = 0L,
    val isLocal: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)
