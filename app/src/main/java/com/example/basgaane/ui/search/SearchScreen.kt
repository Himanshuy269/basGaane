package com.example.basgaane.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.basgaane.domain.model.Song
import com.example.basgaane.viewmodel.HomeTab
import com.example.basgaane.ui.common.LiquidCrystalBackground
import com.example.basgaane.ui.home.HomeTabsCapsule
import com.example.basgaane.ui.theme.PrismCyan
import com.example.basgaane.ui.theme.TextPrimary
import com.example.basgaane.ui.theme.TextTertiary

/**
 * Main Search Screen composable allowing users to discover and filter songs.
 */
@Composable
fun SearchScreen(
    searchQuery: String,
    selectedFilterTag: String,
    onSearchQueryChanged: (String) -> Unit,
    onFilterTagSelected: (String) -> Unit,
    filteredSongs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit,
    selectedTab: HomeTab = HomeTab.SEARCH,
    onTabSelected: ((HomeTab) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val filterTags = remember { listOf("All", "Cloud", "Device", "Synth", "Groove", "Chill", "Acoustic") }

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

            // Title
            Text(
                text = "Search",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = TextPrimary
            )
            Text(
                text = "Find songs by title, artist, or tags",
                style = MaterialTheme.typography.labelMedium,
                color = PrismCyan
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Search Input Field
            SearchHeaderInput(
                searchQuery = searchQuery,
                onSearchQueryChanged = onSearchQueryChanged
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips Row
            SearchFilterChipsRow(
                filterTags = filterTags,
                selectedFilterTag = selectedFilterTag,
                onFilterTagSelected = onFilterTagSelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Results Section
            if (filteredSongs.isEmpty()) {
                SearchEmptyState(
                    searchQuery = searchQuery,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredSongs.size} tracks found",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextTertiary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    itemsIndexed(
                        items = filteredSongs,
                        key = { _, song -> song.id },
                        contentType = { _, _ -> "search_song_item" }
                    ) { index, song ->
                        val isCurrent = song.id == currentSong?.id

                        SearchSongItemCard(
                            index = index + 1,
                            song = song,
                            isSelected = isCurrent,
                            isPlaying = isCurrent && isPlaying,
                            onClick = { onSongClick(song) },
                            onAddToPlaylist = { onAddToPlaylistClick(song) },
                            onDelete = { onDeleteSong(song) }
                        )
                    }
                }
            }
        }
    }
}
