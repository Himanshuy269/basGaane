package com.example.basgaane.domain.repository

import android.net.Uri
import com.example.basgaane.domain.model.Song
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository contract defining music storage and retrieval operations.
 */
interface MusicRepository {
    fun getSongs(): Flow<List<Song>>
    suspend fun addSong(song: Song): Result<Song>
    suspend fun uploadAndAddSong(
        title: String,
        artist: String,
        audioUri: Uri,
        durationMs: Long,
        coverUrl: String? = null
    ): Result<Song>
    suspend fun removeSong(songId: String): Result<Unit>
    fun refreshSongs()
}
