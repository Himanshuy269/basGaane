package com.example.basgaane.ui.playlists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.basgaane.domain.model.Playlist
import com.example.basgaane.viewmodel.HomeTab
import com.example.basgaane.ui.common.LiquidCrystalBackground
import com.example.basgaane.ui.home.HomeTabsCapsule

/**
 * Main Playlists Screen displaying the list of created playlists with option to create new ones.
 */
@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    selectedTab: HomeTab = HomeTab.PLAYLISTS,
    onTabSelected: ((HomeTab) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LiquidCrystalBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Navigation capsule if tabs are provided
            if (onTabSelected != null) {
                HomeTabsCapsule(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Header Action Bar
            PlaylistsHeader(
                onCreatePlaylistClick = onCreatePlaylistClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (playlists.isEmpty()) {
                PlaylistsEmptyState(
                    onCreatePlaylistClick = onCreatePlaylistClick,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(
                        items = playlists,
                        key = { it.id },
                        contentType = { "playlist_card" }
                    ) { playlist ->
                        PlaylistCard(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist) },
                            onDelete = { onDeletePlaylist(playlist) }
                        )
                    }
                }
            }
        }
    }
}
