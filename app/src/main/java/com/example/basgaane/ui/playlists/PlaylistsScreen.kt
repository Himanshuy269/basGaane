package com.example.basgaane.ui.playlists

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
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.basgaane.data.datasource.local.LocalPlaylistDataSource
import com.example.basgaane.domain.model.Playlist
import com.example.basgaane.ui.common.BasGaaneTopBar
import com.example.basgaane.ui.theme.NoctraBorder
import com.example.basgaane.ui.theme.NoctraChipInactive
import com.example.basgaane.ui.theme.NoctraDark
import com.example.basgaane.ui.theme.NoctraLime
import com.example.basgaane.ui.theme.NoctraSurface
import com.example.basgaane.ui.theme.NoctraTextMuted
import com.example.basgaane.ui.theme.NoctraTextPrimary
import com.example.basgaane.ui.theme.NoctraTextSecondary
import com.example.basgaane.ui.theme.PrismMagenta
import com.example.basgaane.ui.theme.PrismViolet
import com.example.basgaane.viewmodel.HomeTab

/**
 * Screen 3: Playlists Screen ("Your rotation.")
 * Matches Screenshot 2 in the Noctra design system.
 */
@Composable
fun PlaylistsScreen(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    onPlayPlaylist: ((Playlist) -> Unit)? = null,
    userName: String = "User",
    isUpdateAvailable: Boolean = false,
    onOpenEditProfileDialog: () -> Unit = {},
    selectedTab: HomeTab = HomeTab.PLAYLISTS,
    onTabSelected: ((HomeTab) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("By you") }
    val filterOptions = remember { listOf("By you", "Saved", "Downloaded") }

    // Pinned mix: Liked Songs or first playlist
    val pinnedPlaylist = remember(playlists) {
        playlists.find {
            it.id == LocalPlaylistDataSource.LIKED_SONGS_PLAYLIST_ID ||
                    it.name.equals("Liked Songs", ignoreCase = true)
        } ?: playlists.firstOrNull()
    }

    val remainingPlaylists = remember(playlists, pinnedPlaylist) {
        if (pinnedPlaylist != null) {
            playlists.filter { it.id != pinnedPlaylist.id }
        } else {
            playlists
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NoctraDark)
    ) {
        // Global Header: Audio wave logo + "basGaane" + Profile Avatar
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
                    text = "SOUNDTRACKS, SHAPED BY YOU",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NoctraTextMuted,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your rotation.",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NoctraTextPrimary
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Filter Chips
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
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Pinned Mix Card ("Blue hour" / "Liked Songs")
            if (pinnedPlaylist != null) {
                item(key = "pinned_mix") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(NoctraSurface)
                            .clickable { onPlaylistClick(pinnedPlaylist) }
                            .padding(18.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Square artwork cover
                                val isLikedPlaylist = pinnedPlaylist.id == LocalPlaylistDataSource.LIKED_SONGS_PLAYLIST_ID ||
                                        pinnedPlaylist.name.equals("Liked Songs", ignoreCase = true)

                                Box(
                                    modifier = Modifier
                                        .size(116.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isLikedPlaylist) {
                                                Brush.linearGradient(listOf(PrismMagenta, PrismViolet))
                                            } else {
                                                Brush.linearGradient(listOf(Color(0xFF1E2838), Color(0xFF141924)))
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isLikedPlaylist) {
                                        Icon(
                                            imageVector = Icons.Default.Favorite,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(46.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                                            contentDescription = null,
                                            tint = NoctraLime,
                                            modifier = Modifier.size(44.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Playlist Info & Action Row
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Pinned mix label
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PushPin,
                                            contentDescription = null,
                                            tint = NoctraLime,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "PINNED MIX",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NoctraLime,
                                            letterSpacing = 1.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = pinnedPlaylist.name,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NoctraTextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "${pinnedPlaylist.songIds.size} tracks • Curated mix",
                                        fontSize = 12.sp,
                                        color = NoctraTextSecondary
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Action buttons row: Play FAB + Download + More
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Play FAB
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(NoctraLime)
                                                .clickable {
                                                    onPlayPlaylist?.invoke(pinnedPlaylist) ?: onPlaylistClick(pinnedPlaylist)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Play",
                                                tint = Color(0xFF0C0E12),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        // Download Icon Button
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF1D2230))
                                                .clickable { /* Download for offline */ },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDownward,
                                                contentDescription = "Download",
                                                tint = NoctraTextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        // More Icon Button
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF1D2230))
                                                .clickable { onPlaylistClick(pinnedPlaylist) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MoreHoriz,
                                                contentDescription = "More",
                                                tint = NoctraTextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Subtitle description below
                            val descText = pinnedPlaylist.description.ifBlank {
                                "For the space between the last train and sunrise."
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = descText,
                                fontSize = 12.sp,
                                color = NoctraTextMuted,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Section Header: "Made by you" + "+ New playlist"
            item(key = "section_header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Made by you",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NoctraTextPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onCreatePlaylistClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New playlist",
                            tint = NoctraLime,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "New playlist",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NoctraLime
                        )
                    }
                }
            }

            // Playlists List
            if (remainingPlaylists.isEmpty() && pinnedPlaylist == null) {
                item(key = "empty_state") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No playlists yet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NoctraTextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap '+ New playlist' above to create your first mix",
                                fontSize = 13.sp,
                                color = NoctraTextMuted
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = remainingPlaylists,
                    key = { _, pl -> pl.id },
                    contentType = { _, _ -> "playlist_item" }
                ) { _, playlist ->
                    val isLiked = playlist.id == LocalPlaylistDataSource.LIKED_SONGS_PLAYLIST_ID ||
                            playlist.name.equals("Liked Songs", ignoreCase = true)

                    NoctraPlaylistItem(
                        playlist = playlist,
                        isLikedPlaylist = isLiked,
                        onClick = { onPlaylistClick(playlist) },
                        onDelete = { onDeletePlaylist(playlist) }
                    )
                }
            }
        }
    }
}

/**
 * Minimalist Playlist Item matching Screenshot 2.
 */
@Composable
fun NoctraPlaylistItem(
    playlist: Playlist,
    isLikedPlaylist: Boolean,
    onClick: () -> Unit,
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
        // Square playlist artwork
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        if (isLikedPlaylist) {
                            listOf(PrismMagenta, PrismViolet)
                        } else {
                            listOf(Color(0xFF1E2330), Color(0xFF151922))
                        }
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLikedPlaylist) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = null,
                    tint = NoctraLime,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title & Track Count
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = NoctraTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = NoctraLime,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${playlist.songIds.size} tracks • Updated recently",
                    fontSize = 12.sp,
                    color = NoctraTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 3-dot options menu (protected for Liked Songs)
        if (!isLikedPlaylist) {
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
                        text = { Text("Delete Playlist", color = MaterialTheme.colorScheme.error) },
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
}
