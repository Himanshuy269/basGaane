package com.example.basgaane.domain.model

import java.util.UUID

/**
 * Domain entity representing a curated playlist.
 * Allows users and friends to group tracks by mood, genre, or personal taste.
 */
data class Playlist(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val songIds: List<String> = emptyList(),
    val coverUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
