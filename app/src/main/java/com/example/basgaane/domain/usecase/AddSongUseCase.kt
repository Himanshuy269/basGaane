package com.example.basgaane.domain.usecase

import android.net.Uri
import com.example.basgaane.domain.model.Song
import com.example.basgaane.domain.repository.MusicRepository

/**
 * Use case to import and store a new audio track into the library.
 */
class AddSongUseCase(
    private val repository: MusicRepository
) {
    suspend operator fun invoke(
        title: String,
        artist: String,
        audioUri: Uri,
        durationMs: Long,
        coverUrl: String? = null
    ): Result<Song> {
        val cleanTitle = title.trim().ifEmpty { "New Track" }
        val cleanArtist = artist.trim().ifEmpty { "My Music" }

        return repository.uploadAndAddSong(
            title = cleanTitle,
            artist = cleanArtist,
            audioUri = audioUri,
            durationMs = durationMs,
            coverUrl = coverUrl
        )
    }
}
