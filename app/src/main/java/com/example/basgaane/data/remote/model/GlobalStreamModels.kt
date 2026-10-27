package com.example.basgaane.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Invidious / Global Stream search result model.
 */
data class InvidiousVideoItem(
    @SerializedName("type") val type: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("videoId") val videoId: String? = null,
    @SerializedName("author") val author: String? = null,
    @SerializedName("authorVerified") val authorVerified: Boolean? = false,
    @SerializedName("lengthSeconds") val lengthSeconds: Long? = 0L,
    @SerializedName("viewCount") val viewCount: Long? = 0L
)

/**
 * Invidious video stream resolution model.
 */
data class InvidiousVideoDetail(
    @SerializedName("title") val title: String? = null,
    @SerializedName("adaptiveFormats") val adaptiveFormats: List<InvidiousFormatItem>? = emptyList(),
    @SerializedName("formatStreams") val formatStreams: List<InvidiousFormatItem>? = emptyList()
)

data class InvidiousFormatItem(
    @SerializedName("itag") val itag: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("bitrate") val bitrate: String? = null,
    @SerializedName("container") val container: String? = null
)
