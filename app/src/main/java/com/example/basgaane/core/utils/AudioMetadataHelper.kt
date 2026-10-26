package com.example.basgaane.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.Collections

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

    // High-speed in-memory LRU cache for extracted album pictures (max 64 items)
    private val memoryCoverCache = LruCache<String, ByteArray>(64)

    // Negative cache of songs verified to have NO embedded art (prevents repeated MediaMetadataRetriever runs on scroll)
    private val noCoverCache: MutableSet<String> = Collections.synchronizedSet(HashSet())

    /**
     * Fast non-blocking lookup: returns cover URL if valid, or existing local File, or null.
     */
    fun getCoverModelOrFile(context: Context, songId: String, coverUrl: String?): Any? {
        if (!coverUrl.isNullOrEmpty()) {
            return coverUrl
        }
        val localCover = File(context.filesDir, "covers/cover_${songId}.jpg")
        return if (localCover.exists() && localCover.length() > 0) {
            localCover
        } else {
            null
        }
    }

    /**
     * Checks if this song ID has already been confirmed to have no embedded artwork.
     */
    fun hasNoEmbeddedCover(songId: String): Boolean {
        return noCoverCache.contains(songId)
    }

    /**
     * Resolves artwork on [Dispatchers.IO]: checks local file, checks memory cache,
     * extracts via MediaMetadataRetriever once, and caches the result.
     */
    suspend fun resolveAndCacheCover(context: Context, songId: String, uriString: String): Any? = withContext(Dispatchers.IO) {
        // 1. Check local file on disk
        val localCover = File(context.filesDir, "covers/cover_${songId}.jpg")
        if (localCover.exists() && localCover.length() > 0) {
            return@withContext localCover
        }

        // 2. Check negative cache
        if (noCoverCache.contains(songId) || noCoverCache.contains(uriString)) {
            return@withContext null
        }

        // 3. Check memory LRU cache
        val inMemory = memoryCoverCache.get(songId) ?: memoryCoverCache.get(uriString)
        if (inMemory != null) {
            val savedPath = saveCoverLocally(context, songId, inMemory)
            return@withContext savedPath?.let { File(context.filesDir, "covers/cover_${songId}.jpg") } ?: inMemory
        }

        // 4. Extract via MediaMetadataRetriever
        val extractedBytes = extractEmbeddedPicture(context, uriString)
        if (extractedBytes != null && extractedBytes.isNotEmpty()) {
            memoryCoverCache.put(songId, extractedBytes)
            memoryCoverCache.put(uriString, extractedBytes)
            saveCoverLocally(context, songId, extractedBytes)
            return@withContext File(context.filesDir, "covers/cover_${songId}.jpg")
        } else {
            noCoverCache.add(songId)
            noCoverCache.add(uriString)
            return@withContext null
        }
    }

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
        val cached = memoryCoverCache.get(uriString)
        if (cached != null) return cached
        if (noCoverCache.contains(uriString)) return null

        val retriever = MediaMetadataRetriever()
        return try {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "content" || uri.scheme == "file") {
                retriever.setDataSource(context, uri)
            } else {
                retriever.setDataSource(uriString, HashMap())
            }
            val bytes = retriever.embeddedPicture
            if (bytes != null && bytes.isNotEmpty()) {
                memoryCoverCache.put(uriString, bytes)
                bytes
            } else {
                noCoverCache.add(uriString)
                null
            }
        } catch (e: Exception) {
            noCoverCache.add(uriString)
            null
        } finally {
            try {
                retriever.release()
            } catch (ignored: Exception) {}
        }
    }

    /**
     * Saves picture byte array to local internal storage as an optimized, downsampled thumbnail.
     */
    fun saveCoverLocally(context: Context, songId: String, pictureBytes: ByteArray): String? {
        return try {
            val coversDir = File(context.filesDir, "covers")
            if (!coversDir.exists()) {
                coversDir.mkdirs()
            }
            val coverFile = File(coversDir, "cover_${songId}.jpg")

            // Downscale to max 512px thumbnail to keep memory footprint minuscule
            val downsampled = try {
                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(pictureBytes, 0, pictureBytes.size, opts)
                var inSampleSize = 1
                val maxDim = maxOf(opts.outWidth, opts.outHeight)
                while (maxDim / inSampleSize > 512) {
                    inSampleSize *= 2
                }
                val decodeOpts = BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                    inPreferredConfig = Bitmap.Config.RGB_565 // 50% RAM savings compared to ARGB_8888
                }
                val bitmap = BitmapFactory.decodeByteArray(pictureBytes, 0, pictureBytes.size, decodeOpts)
                if (bitmap != null) {
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                    bitmap.recycle()
                    stream.toByteArray()
                } else {
                    pictureBytes
                }
            } catch (e: Exception) {
                pictureBytes
            }

            FileOutputStream(coverFile).use { out ->
                out.write(downsampled)
                out.flush()
            }
            Uri.fromFile(coverFile).toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save cover art locally for $songId: ${e.message}", e)
            null
        }
    }
}
