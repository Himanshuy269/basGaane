package com.example.basgaane.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Encapsulates screen destination metadata including routes, titles, and icons.
 */
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : Screen(
        route = NavigationRoutes.HOME,
        title = "All Tracks",
        icon = Icons.Default.Headphones
    )

    data object Search : Screen(
        route = NavigationRoutes.SEARCH,
        title = "Search",
        icon = Icons.Default.Search
    )

    data object Playlists : Screen(
        route = NavigationRoutes.PLAYLISTS,
        title = "Playlists",
        icon = Icons.AutoMirrored.Filled.QueueMusic
    )

    data object PlaylistDetail : Screen(
        route = NavigationRoutes.PLAYLIST_DETAIL,
        title = "Playlist Detail",
        icon = Icons.AutoMirrored.Filled.QueueMusic
    )

    companion object {
        val bottomNavTabs = listOf(Home, Search, Playlists)
    }
}
