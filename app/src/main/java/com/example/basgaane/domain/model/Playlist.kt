package com.example.basgaane.domain.model

import androidx.compose.runtime.Immutable
import java.util.UUID

/**
 * Domain entity representing a curated playlist.
 * Marked @Immutable for zero-recomposition scrolling in playlist lists.
 */
@Immutable
data class Playlist(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val songIds: List<String> = emptyList(),
    val coverUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
