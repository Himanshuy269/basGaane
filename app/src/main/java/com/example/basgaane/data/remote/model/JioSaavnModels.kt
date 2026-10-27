package com.example.basgaane.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Root response from JioSaavn song search query.
 */
data class JioSaavnSearchResponse(
    @SerializedName("total") val total: Int? = 0,
    @SerializedName("start") val start: Int? = 1,
    @SerializedName("results") val results: List<JioSaavnSongItem>? = emptyList()
)

/**
 * Represents a single search result item from JioSaavn.
 */
data class JioSaavnSongItem(
    @SerializedName("id") val id: String? = null,
    @SerializedName("title") val rawTitle: String? = null,
    @SerializedName("subtitle") val subtitle: String? = null,
    @SerializedName("image") val rawImage: String? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("year") val year: String? = null,
    @SerializedName("play_count") val playCount: String? = null,
    @SerializedName("more_info") val moreInfo: JioSaavnMoreInfo? = null
) {
    /**
     * Cleaned song title with HTML entities unescaped.
     */
    val cleanTitle: String
        get() = cleanHtml(rawTitle ?: "Unknown Track")

    /**
     * Cleaned artist name extracted from subtitle or primary artists map.
     */
    val cleanArtist: String
        get() {
            val primaryArtists = moreInfo?.artistMap?.primaryArtists
            if (!primaryArtists.isNullOrEmpty()) {
                val names = primaryArtists.mapNotNull { it.name?.trim() }.filter { it.isNotEmpty() }
                if (names.isNotEmpty()) {
                    return cleanHtml(names.joinToString(", "))
                }
            }
            val artists = moreInfo?.artistMap?.artists
            if (!artists.isNullOrEmpty()) {
                val singers = artists.filter { it.role == "singer" || it.role == "primary_artists" }
                val names = singers.mapNotNull { it.name?.trim() }.filter { it.isNotEmpty() }
                if (names.isNotEmpty()) {
                    return cleanHtml(names.joinToString(", "))
                }
            }
            // Subtitle often formatted as "Artist - Album"
            val sub = subtitle?.trim()
            if (!sub.isNullOrBlank()) {
                val parts = sub.split("-")
                if (parts.isNotEmpty() && parts[0].isNotBlank()) {
                    return cleanHtml(parts[0].trim())
                }
            }
            val music = moreInfo?.music?.trim()
            if (!music.isNullOrBlank()) {
                return cleanHtml(music)
            }
            val album = moreInfo?.album?.trim()
            if (!album.isNullOrBlank()) {
                return cleanHtml(album)
            }
            return "Unknown Artist"
        }

    /**
     * High definition 500x500 album art URL.
     */
    val hdCoverUrl: String?
        get() = rawImage
            ?.replace("150x150", "500x500")
            ?.replace("http://", "https://")

    /**
     * Duration in milliseconds.
     */
    val durationMs: Long
        get() {
            val sec = moreInfo?.duration?.toLongOrNull() ?: 0L
            return sec * 1000L
        }

    /**
     * Encrypted media URL token needed to generate stream URL.
     */
    val encryptedMediaUrl: String?
        get() = moreInfo?.encryptedMediaUrl

    private fun cleanHtml(text: String): String {
        return text
            .replace("&quot;", "\"")
            .replace("&amp;", "&")
            .replace("&#039;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .trim()
    }
}

data class JioSaavnMoreInfo(
    @SerializedName("duration") val duration: String? = null,
    @SerializedName("album") val album: String? = null,
    @SerializedName("music") val music: String? = null,
    @SerializedName("encrypted_media_url") val encryptedMediaUrl: String? = null,
    @SerializedName("artistMap") val artistMap: JioSaavnArtistMap? = null
)

data class JioSaavnArtistMap(
    @SerializedName("primary_artists") val primaryArtists: List<JioSaavnArtist>? = emptyList(),
    @SerializedName("artists") val artists: List<JioSaavnArtist>? = emptyList()
)

data class JioSaavnArtist(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("role") val role: String? = null
)

/**
 * Response returned from song.generateAuthToken.
 */
data class JioSaavnAuthTokenResponse(
    @SerializedName("auth_url") val authUrl: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("status") val status: String? = null
)
