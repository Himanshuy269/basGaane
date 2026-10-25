package com.example.basgaane.model

import java.util.UUID

/**
 * Represents a musical track in BasGaane.
 *
 * @param id Unique identifier for the track.
 * @param title Song title (e.g. "Neon Nights").
 * @param artist Artist or creator name.
 * @param audioUrl URL or local content URI string where the audio file is located.
 * @param coverUrl Optional URL or local URI string for album artwork.
 * @param durationMs Duration of track in milliseconds.
 * @param isLocal Whether this song was added locally from device storage or streamed from cloud.
 * @param addedAt Timestamp when song was added.
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
