package com.example.basgaane.navigation

/**
 * Route path constants for application destinations.
 */
object NavigationRoutes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val PLAYLISTS = "playlists"
    const val PLAYLIST_DETAIL = "playlist_detail/{playlistId}"

    fun playlistDetail(playlistId: String): String = "playlist_detail/$playlistId"
}
