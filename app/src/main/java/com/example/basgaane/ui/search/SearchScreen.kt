package com.example.basgaane.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.basgaane.domain.model.Song
import com.example.basgaane.ui.common.BasGaaneTopBar
import com.example.basgaane.ui.common.EqualizerBars
import com.example.basgaane.ui.common.SongCoverImage
import com.example.basgaane.ui.theme.NoctraBorder
import com.example.basgaane.ui.theme.NoctraChipInactive
import com.example.basgaane.ui.theme.NoctraDark
import com.example.basgaane.ui.theme.NoctraLime
import com.example.basgaane.ui.theme.NoctraSurface
import com.example.basgaane.ui.theme.NoctraTextMuted
import com.example.basgaane.ui.theme.NoctraTextPrimary
import com.example.basgaane.ui.theme.NoctraTextSecondary
import com.example.basgaane.viewmodel.HomeTab
import java.util.Locale

/**
 * Screen 4: Search Screen ("Find your sound.")
 * Matches Screenshot 1 in the Noctra design system.
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
    userName: String = "User",
    isUpdateAvailable: Boolean = false,
    onOpenEditProfileDialog: () -> Unit = {},
    onSongClick: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit,
    selectedTab: HomeTab = HomeTab.SEARCH,
    onTabSelected: ((HomeTab) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val filterTabs = remember { listOf("All", "Tracks", "Albums", "Artists") }

    // Top Result: first match if query is present
    val topResult = remember(filteredSongs, searchQuery) {
        if (searchQuery.isNotBlank() && filteredSongs.isNotEmpty()) filteredSongs.first() else null
    }

    val remainingTracks = remember(filteredSongs, topResult) {
        if (topResult != null && filteredSongs.size > 1) {
            filteredSongs.drop(1)
        } else {
            filteredSongs
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NoctraDark)
    ) {
        // Global Header: Audio wave logo + "bacGaane" + Profile Avatar
        BasGaaneTopBar(
            userName = userName,
            isUpdateAvailable = isUpdateAvailable,
            onOpenEditProfileDialog = onOpenEditProfileDialog
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 150.dp)
        ) {
            // Eyebrow & Hero Title
            item(key = "hero_header") {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "EVERY TRACK. EVERYWHERE.",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NoctraTextMuted,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Find your sound.",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NoctraTextPrimary
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Search Bar Input Field with lime accent
            item(key = "search_input") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = {
                        Text(
                            text = "Search songs, artists, albums...",
                            fontSize = 14.sp,
                            color = NoctraTextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = if (searchQuery.isNotEmpty()) NoctraLime else NoctraTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = NoctraTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { keyboardController?.hide() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = NoctraSurface,
                        unfocusedContainerColor = NoctraSurface,
                        focusedBorderColor = NoctraLime,
                        unfocusedBorderColor = NoctraBorder,
                        focusedTextColor = NoctraTextPrimary,
                        unfocusedTextColor = NoctraTextPrimary,
                        cursorColor = NoctraLime
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Filter Chips: All, Tracks, Albums, Artists
            item(key = "filter_chips") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filterTabs.forEach { tabName ->
                        val isSelected = selectedFilterTag.equals(tabName, ignoreCase = true) ||
                                (selectedFilterTag == "All" && tabName == "All")
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) NoctraLime else NoctraChipInactive)
                                .clickable { onFilterTagSelected(tabName) }
                                .padding(horizontal = 18.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = tabName,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF0C0E12) else Color(0xFFC5CBD8)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Top Result Card (if searching and has results)
            if (topResult != null) {
                item(key = "top_result") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(NoctraSurface)
                            .clickable { onSongClick(topResult) }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Cover image
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E2330))
                            ) {
                                SongCoverImage(
                                    song = topResult,
                                    modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "TOP RESULT",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NoctraLime,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = topResult.title,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NoctraTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${topResult.artist} • Track",
                                    fontSize = 12.sp,
                                    color = NoctraTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Circular Play Button
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(NoctraLime)
                                    .clickable { onSongClick(topResult) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Top Result",
                                    tint = Color(0xFF0C0E12),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Section Header: "Tracks" + "See all X"
            item(key = "section_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tracks",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NoctraTextPrimary
                    )

                    if (filteredSongs.isNotEmpty()) {
                        Text(
                            text = "See all ${filteredSongs.size}",
                            fontSize = 13.sp,
                            color = NoctraTextMuted
                        )
                    }
                }
            }

            // Track Items
            if (filteredSongs.isEmpty()) {
                item(key = "empty_state") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (searchQuery.isBlank()) "Search your music" else "No matching tracks found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NoctraTextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try searching by title, artist, or genre",
                                fontSize = 13.sp,
                                color = NoctraTextMuted
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = remainingTracks,
                    key = { _, song -> song.id },
                    contentType = { _, _ -> "search_song" }
                ) { _, song ->
                    val isSelected = song.id == currentSong?.id
                    NoctraSearchSongItem(
                        song = song,
                        isSelected = isSelected,
                        isPlaying = isSelected && isPlaying,
                        onClick = { onSongClick(song) },
                        onAddToPlaylist = { onAddToPlaylistClick(song) },
                        onDelete = { onDeleteSong(song) }
                    )
                }
            }

            // Footer indicator: "Searching streaming + your device"
            item(key = "footer_indicator") {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Podcasts,
                        contentDescription = null,
                        tint = NoctraTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Searching streaming + your device",
                        fontSize = 12.sp,
                        color = NoctraTextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun NoctraSearchSongItem(
    song: Song,
    isSelected: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Square Artwork
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1E2330))
        ) {
            SongCoverImage(
                song = song,
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(10.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title & Artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) NoctraLime else NoctraTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = NoctraTextMuted,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${song.artist} • ${if (song.isLocal) "Device" else "Cloud"}",
                    fontSize = 12.sp,
                    color = NoctraTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Duration or Equalizer
        if (isPlaying) {
            EqualizerBars(
                isPlaying = true,
                barColor = NoctraLime,
                modifier = Modifier.padding(end = 4.dp)
            )
        } else if (song.durationMs > 0) {
            Text(
                text = formatDurationSearch(song.durationMs),
                fontSize = 12.sp,
                color = NoctraTextMuted
            )
        }

        // 3-dot options menu
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Options",
                    tint = NoctraTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Add to Playlist", color = NoctraTextPrimary) },
                    leadingIcon = {
                        Icon(
                            Icons.AutoMirrored.Filled.PlaylistAdd,
                            contentDescription = null,
                            tint = NoctraLime
                        )
                    },
                    onClick = {
                        showMenu = false
                        onAddToPlaylist()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete Track", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}

private fun formatDurationSearch(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
