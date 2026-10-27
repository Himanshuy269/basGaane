package com.example.basgaane.ui.home

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.basgaane.core.update.AppUpdateInfo
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
import com.example.basgaane.viewmodel.ThemeMode
import java.util.Locale

/**
 * Screen 1: Local Storage Screen ("All yours.")
 * Matches Screenshot 4 in the Noctra design system.
 */
@Composable
fun HomeScreen(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    userName: String,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onSongClick: (Song) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit,
    onAddMusicClick: () -> Unit,
    onOpenEditProfileDialog: () -> Unit,
    updateInfo: AppUpdateInfo? = null,
    onOpenAppUpdateDialog: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    bottomBar: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("Tracks") }
    val filterOptions = remember { listOf("Tracks", "Albums", "Artists", "Folders") }

    val displayedSongs = remember(songs, selectedFilter) {
        when (selectedFilter) {
            "Albums" -> songs.sortedBy { it.addedBy }
            "Artists" -> songs.sortedBy { it.artist.lowercase() }
            "Folders" -> songs.filter { it.isLocal }
            else -> songs
        }
    }

    // Dynamic storage calculations
    val estimatedGb = remember(songs.size) {
        val totalMb = songs.size * 28.5
        val gb = totalMb / 1024.0
        String.format(Locale.US, "%.1f", gb.coerceAtLeast(0.1))
    }

    Scaffold(
        containerColor = NoctraDark,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = NoctraSurface,
                    contentColor = NoctraTextPrimary,
                    actionColor = NoctraLime,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        bottomBar = bottomBar,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NoctraDark)
                .padding(innerPadding)
        ) {
            // Global Header: Audio wave logo + "basGaane" + Profile Avatar
            BasGaaneTopBar(
                userName = userName,
                isUpdateAvailable = updateInfo != null,
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
                        text = "YOUR OFFLINE COLLECTION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NoctraTextMuted,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "All yours.",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = NoctraTextPrimary
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Update Banner if available
                if (updateInfo != null) {
                    item(key = "update_banner") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(NoctraSurface)
                                .clickable { onOpenAppUpdateDialog() }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.RocketLaunch,
                                        contentDescription = null,
                                        tint = NoctraLime,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Update available: v${updateInfo.latestVersionName}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NoctraTextPrimary
                                    )
                                }
                                Text(
                                    text = "Update",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NoctraLime
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Device Storage Summary Card
                item(key = "device_card") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(NoctraSurface)
                            .padding(horizontal = 18.dp, vertical = 18.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "ON THIS DEVICE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = NoctraTextMuted,
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${songs.size} tracks • $estimatedGb GB",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NoctraTextPrimary
                                    )
                                }

                                // + Import Button
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFF1D212D))
                                        .clickable { onAddMusicClick() }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Import",
                                            tint = NoctraLime,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Import",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NoctraTextPrimary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Storage indicator line
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF242A38))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.28f)
                                            .height(4.dp)
                                            .clip(CircleShape)
                                            .background(NoctraLime)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "18.6 GB free",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = NoctraTextMuted
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Filter Chips Row
                item(key = "filter_chips") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filterOptions.forEach { filter ->
                            val isSelected = selectedFilter == filter
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) NoctraLime else NoctraChipInactive)
                                    .clickable { selectedFilter = filter }
                                    .padding(horizontal = 18.dp, vertical = 9.dp)
                            ) {
                                Text(
                                    text = filter,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF0C0E12) else Color(0xFFC5CBD8)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Section Header: "Recently added" + Sort Icon
                item(key = "section_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recently added",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NoctraTextPrimary
                        )

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort",
                            tint = NoctraTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Song List Items
                if (displayedSongs.isEmpty()) {
                    item(key = "empty_state") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No offline tracks found",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NoctraTextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tap '+ Import' above to add songs from your phone",
                                    fontSize = 13.sp,
                                    color = NoctraTextMuted
                                )
                            }
                        }
                    }
                } else {
                    itemsIndexed(
                        items = displayedSongs,
                        key = { _, song -> song.id },
                        contentType = { _, _ -> "song_item" }
                    ) { _, song ->
                        val isSelected = song.id == currentSong?.id
                        NoctraSongListItem(
                            song = song,
                            isSelected = isSelected,
                            isPlaying = isSelected && isPlaying,
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

/**
 * Minimalist Song List Item matching Screenshot 4.
 */
@Composable
fun NoctraSongListItem(
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
        // Square Album Artwork
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

        // Title and Format/Artist
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
                    text = "${song.artist} • ${if (song.isLocal) "FLAC" else "CLOUD"}",
                    fontSize = 12.sp,
                    color = NoctraTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Right side: Equalizer if playing, or duration
        if (isPlaying) {
            EqualizerBars(
                isPlaying = true,
                barColor = NoctraLime,
                modifier = Modifier.padding(end = 4.dp)
            )
        } else if (song.durationMs > 0) {
            Text(
                text = formatDuration(song.durationMs),
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

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
