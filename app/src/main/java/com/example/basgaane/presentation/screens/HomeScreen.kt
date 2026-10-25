package com.example.basgaane.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import coil.compose.AsyncImage
import com.example.basgaane.R
import com.example.basgaane.domain.model.Playlist
import com.example.basgaane.domain.model.Song
import com.example.basgaane.presentation.components.EqualizerBars
import com.example.basgaane.presentation.components.LiquidCrystalBackground
import com.example.basgaane.presentation.components.MiniPlayer
import com.example.basgaane.presentation.components.crystalGlassCard
import com.example.basgaane.presentation.components.crystalPill
import com.example.basgaane.presentation.components.formatMs
import com.example.basgaane.presentation.theme.AccentGradientEnd
import com.example.basgaane.presentation.theme.AccentGradientStart
import com.example.basgaane.presentation.theme.BackgroundDark
import com.example.basgaane.presentation.theme.NeonCyan
import com.example.basgaane.presentation.theme.NeonPink
import com.example.basgaane.presentation.theme.NeonPurple
import com.example.basgaane.presentation.theme.PrismCyan
import com.example.basgaane.presentation.theme.PrismMagenta
import com.example.basgaane.presentation.theme.PrismViolet
import com.example.basgaane.presentation.theme.SurfaceDark
import com.example.basgaane.presentation.theme.SurfaceDarkVariant
import com.example.basgaane.presentation.theme.TextPrimary
import com.example.basgaane.presentation.theme.TextSecondary
import com.example.basgaane.presentation.theme.TextTertiary
import com.example.basgaane.presentation.viewmodel.HomeTab

@Composable
fun HomeScreen(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    songs: List<Song>,
    filteredSongs: List<Song>,
    searchQuery: String,
    searchFilterTag: String,
    onSearchQueryChanged: (String) -> Unit,
    onSearchFilterTagChanged: (String) -> Unit,
    playlists: List<Playlist>,
    selectedPlaylist: Playlist?,
    onSelectPlaylist: (Playlist?) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    onAddToPlaylistClick: (Song) -> Unit,
    onRemoveSongFromPlaylist: (playlistId: String, songId: String) -> Unit,
    onPlayPlaylist: (List<Song>, Song?) -> Unit,
    currentSong: Song?,
    isPlaying: Boolean,
    progressMs: Long,
    durationMs: Long,
    onSongClick: (Song) -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onMiniPlayerClick: () -> Unit,
    onAddMusicClick: () -> Unit,
    onDeleteSong: (Song) -> Unit,
    userName: String = "Friend",
    onOpenEditProfileDialog: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = BackgroundDark,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = SurfaceDarkVariant,
                    contentColor = TextPrimary,
                    actionColor = NeonCyan,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        floatingActionButton = {
            if (selectedPlaylist == null && selectedTab == HomeTab.HOME) {
                ExtendedFloatingActionButton(
                    onClick = onAddMusicClick,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add Music", tint = Color.White) },
                    text = { Text("Add Music", fontWeight = FontWeight.Bold, color = Color.White) },
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(bottom = if (currentSong != null) 76.dp else 16.dp)
                        .crystalPill(shape = RoundedCornerShape(20.dp), isActive = true)
                )
            }
        },
        bottomBar = {
            Box(modifier = Modifier.navigationBarsPadding()) {
                MiniPlayer(
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    progressMs = progressMs,
                    durationMs = durationMs,
                    onPlayPause = onPlayPause,
                    onSkipNext = onSkipNext,
                    onClick = onMiniPlayerClick
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LiquidCrystalBackground(modifier = Modifier.fillMaxSize()) {
            // If a playlist is selected, show the full detail screen
            if (selectedPlaylist != null) {
                PlaylistDetailScreen(
                    playlist = selectedPlaylist,
                    allSongs = songs,
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    onBack = { onSelectPlaylist(null) },
                    onPlayAll = { plSongs -> onPlayPlaylist(plSongs, null) },
                    onShufflePlay = { plSongs -> onPlayPlaylist(plSongs.shuffled(), null) },
                    onSongClick = { targetSong, plSongs -> onPlayPlaylist(plSongs, targetSong) },
                    onRemoveSongFromPlaylist = { sId -> onRemoveSongFromPlaylist(selectedPlaylist.id, sId) },
                    onDeletePlaylist = {
                        onDeletePlaylist(selectedPlaylist)
                        onSelectPlaylist(null)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Top Branding Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.app_logo),
                                contentDescription = "BasGaane Logo",
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = 1.dp,
                                        brush = Brush.linearGradient(listOf(PrismCyan, PrismViolet)),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "BasGaane",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Play & Share Your Music",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PrismCyan
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // User Profile Pill Button
                            Box(
                                modifier = Modifier
                                    .crystalPill(
                                        shape = RoundedCornerShape(14.dp),
                                        isActive = true,
                                        onClick = onOpenEditProfileDialog
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Edit Profile",
                                        tint = PrismCyan,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = userName,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Track count chip
                            Box(
                                modifier = Modifier
                                    .crystalPill(shape = RoundedCornerShape(14.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${songs.size} tracks",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Liquid Crystal Floating Segmented Capsule Navigation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .crystalPill(shape = RoundedCornerShape(24.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HomeTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            val (title, icon) = when (tab) {
                                HomeTab.HOME -> "All Tracks" to Icons.Default.Headphones
                                HomeTab.SEARCH -> "Search" to Icons.Default.Search
                                HomeTab.PLAYLISTS -> "Playlists" to Icons.AutoMirrored.Filled.QueueMusic
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .crystalPill(
                                        shape = RoundedCornerShape(20.dp),
                                        isActive = isSelected,
                                        onClick = { onTabSelected(tab) }
                                    )
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else TextTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Body Section based on active tab
                    when (selectedTab) {
                        HomeTab.HOME -> {
                            AllTracksContent(
                                songs = songs,
                                currentSong = currentSong,
                                isPlaying = isPlaying,
                                onSongClick = onSongClick,
                                onAddToPlaylist = onAddToPlaylistClick,
                                onDeleteSong = onDeleteSong
                            )
                        }

                        HomeTab.SEARCH -> {
                            SearchScreen(
                                searchQuery = searchQuery,
                                selectedFilterTag = searchFilterTag,
                                onSearchQueryChanged = onSearchQueryChanged,
                                onFilterTagSelected = onSearchFilterTagChanged,
                                filteredSongs = filteredSongs,
                                currentSong = currentSong,
                                isPlaying = isPlaying,
                                onSongClick = onSongClick,
                                onAddToPlaylistClick = onAddToPlaylistClick,
                                onDeleteSong = onDeleteSong
                            )
                        }

                        HomeTab.PLAYLISTS -> {
                            PlaylistsScreen(
                                playlists = playlists,
                                onPlaylistClick = onSelectPlaylist,
                                onCreatePlaylistClick = onCreatePlaylistClick,
                                onDeletePlaylist = onDeletePlaylist
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun AllTracksContent(
    songs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onSongClick: (Song) -> Unit,
    onAddToPlaylist: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit
) {
    if (songs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No songs added yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap '+ Add Music' below to upload audio files",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 90.dp)
        ) {
            itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                val isCurrentPlaying = song.id == currentSong?.id

                SongItemCard(
                    index = index + 1,
                    song = song,
                    isSelected = isCurrentPlaying,
                    isPlaying = isCurrentPlaying && isPlaying,
                    onClick = { onSongClick(song) },
                    onAddToPlaylist = { onAddToPlaylist(song) },
                    onDelete = { onDeleteSong(song) }
                )
            }
        }
    }
}

@Composable
fun SongItemCard(
    index: Int,
    song: Song,
    isSelected: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .crystalGlassCard(
                shape = RoundedCornerShape(16.dp),
                isSelected = isSelected,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) {
                            Brush.linearGradient(listOf(PrismViolet, PrismCyan))
                        } else {
                            Brush.linearGradient(listOf(Color(0xFF1E2638), Color(0xFF131826)))
                        }
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            if (isSelected) listOf(PrismCyan, PrismMagenta)
                            else listOf(Color(0x33FFFFFF), Color(0x11FFFFFF))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!song.coverUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = song.coverUrl,
                        contentDescription = song.title,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else if (isSelected && isPlaying) {
                    EqualizerBars(
                        isPlaying = true,
                        barColor = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else TextTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) PrismCyan else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .crystalPill(
                                shape = RoundedCornerShape(6.dp),
                                isActive = !song.isLocal
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = if (song.isLocal) Icons.Default.PhoneAndroid else Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = if (song.isLocal) TextTertiary else PrismCyan,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (song.isLocal) "Device" else "Cloud",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = if (song.isLocal) TextTertiary else PrismCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                // Friend attribution badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .crystalPill(
                            shape = RoundedCornerShape(6.dp),
                            isActive = false
                        )
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PrismMagenta,
                        modifier = Modifier.size(9.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Added by ${song.addedBy}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (song.durationMs > 0) {
                Text(
                    text = formatMs(song.durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Add to Playlist", color = TextPrimary) },
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Filled.PlaylistAdd,
                                contentDescription = null,
                                tint = PrismCyan
                            )
                        },
                        onClick = {
                            showMenu = false
                            onAddToPlaylist()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Delete Track", color = NeonPink) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = NeonPink
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
