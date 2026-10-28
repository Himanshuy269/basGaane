package com.example.basgaane.core.util

import android.content.Context
import android.content.Intent
import com.example.basgaane.data.remote.model.JioSaavnSongItem
import com.example.basgaane.domain.model.Song

/**
 * Utility for sharing music tracks and online streaming songs across external apps.
 */
object ShareUtils {

    /**
     * Shares a local or playing Song via Android's native share sheet.
     */
    fun shareSong(context: Context, song: Song) {
        val shareText = buildSongShareText(song)
        launchShareIntent(context, title = song.title, text = shareText)
    }

    /**
     * Shares an online streaming catalog item (JioSaavn / Global Invidious) via native share sheet.
     */
    fun shareOnlineItem(context: Context, item: JioSaavnSongItem) {
        val shareText = buildOnlineItemShareText(item)
        launchShareIntent(context, title = item.cleanTitle, text = shareText)
    }

    private fun buildSongShareText(song: Song): String {
        val sb = StringBuilder()
        sb.append("🎵 Listening to \"${song.title}\" by ${song.artist}\n")

        val token = song.audioUrl
        if (!token.isNullOrBlank()) {
            if (token.startsWith("yt:")) {
                val videoId = token.removePrefix("yt:").trim()
                sb.append("\n▶️ Listen here: https://youtu.be/$videoId\n")
            } else if (token.startsWith("http") && !token.contains("127.0.0.1") && !token.contains("localhost")) {
                sb.append("\n▶️ Stream: $token\n")
            }
        }

        sb.append("\nShared via basGaane 🎶")
        return sb.toString()
    }

    private fun buildOnlineItemShareText(item: JioSaavnSongItem): String {
        val sb = StringBuilder()
        sb.append("🎵 Check out \"${item.cleanTitle}\" by ${item.cleanArtist}\n")

        val token = item.encryptedMediaUrl
        if (!token.isNullOrBlank()) {
            if (token.startsWith("yt:")) {
                val videoId = token.removePrefix("yt:").trim()
                sb.append("\n▶️ Listen here: https://youtu.be/$videoId\n")
            }
        }

        sb.append("\nShared via basGaane 🎶")
        return sb.toString()
    }

    private fun launchShareIntent(context: Context, title: String, text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val chooser = Intent.createChooser(sendIntent, "Share \"$title\" via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
