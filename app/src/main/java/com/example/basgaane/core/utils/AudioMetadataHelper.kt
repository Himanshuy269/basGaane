package com.example.basgaane.core.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream

/**
 * Data container for metadata extracted directly from an audio file.
 */
data class ExtractedAudioMetadata(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val durationMs: Long = 0L,
    val embeddedPicture: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ExtractedAudioMetadata
        if (title != other.title) return false
        if (artist != other.artist) return false
        if (album != other.album) return false
        if (durationMs != other.durationMs) return false
        if (embeddedPicture != null) {
            if (other.embeddedPicture == null) return false
            if (!embeddedPicture.contentEquals(other.embeddedPicture)) return false
        } else if (other.embeddedPicture != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + (artist?.hashCode() ?: 0)
        result = 31 * result + (album?.hashCode() ?: 0)
        result = 31 * result + durationMs.hashCode()
        result = 31 * result + (embeddedPicture?.contentHashCode() ?: 0)
        return result
    }
}

object AudioMetadataHelper {
    private const val TAG = "AudioMetadataHelper"

    /**
     * Extracts ID3 title, artist, duration, and embedded cover art from an audio URI.
     */
    fun extractMetadata(context: Context, audioUri: Uri): ExtractedAudioMetadata {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, audioUri)

            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?.trim()?.takeIf { it.isNotBlank() }
            val artist = (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST))
                ?.trim()?.takeIf { it.isNotBlank() }
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?.trim()?.takeIf { it.isNotBlank() }
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 0L

            val embeddedPicture = retriever.embeddedPicture

            ExtractedAudioMetadata(
                title = title,
                artist = artist,
                album = album,
                durationMs = durationMs,
                embeddedPicture = embeddedPicture
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not extract metadata from $audioUri: ${e.message}")
            ExtractedAudioMetadata()
        } finally {
            try {
                retriever.release()
            } catch (ignored: Exception) {}
        }
    }

    /**
     * Extracts embedded picture from an audio URI or path string.
     */
    fun extractEmbeddedPicture(context: Context, uriString: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        return try {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "content" || uri.scheme == "file") {
                retriever.setDataSource(context, uri)
            } else {
                retriever.setDataSource(uriString, HashMap())
            }
            retriever.embeddedPicture
        } catch (e: Exception) {
            null
        } finally {
            try {
                retriever.release()
            } catch (ignored: Exception) {}
        }
    }

    /**
     * Saves picture byte array to local internal storage and returns a file:// URI string.
     */
    fun saveCoverLocally(context: Context, songId: String, pictureBytes: ByteArray): String? {
        return try {
            val coversDir = File(context.filesDir, "covers")
            if (!coversDir.exists()) {
                coversDir.mkdirs()
            }
            val coverFile = File(coversDir, "cover_${songId}.jpg")
            FileOutputStream(coverFile).use { out ->
                out.write(pictureBytes)
                out.flush()
            }
            Uri.fromFile(coverFile).toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save cover art locally for $songId: ${e.message}", e)
            null
        }
    }
}
