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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.basgaane.data.remote.model.JioSaavnSongItem
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
 * Screen 4: Unified Search Screen ("Find your sound.")
 * Searches both the online streaming catalog (JioSaavn 30M+ songs) and local device files.
 */
@Composable
fun SearchScreen(
    searchQuery: String,
    selectedFilterTag: String,
    onSearchQueryChanged: (String) -> Unit,
    onFilterTagSelected: (String) -> Unit,
    onPerformOnlineSearch: (String) -> Unit = {},
    filteredSongs: List<Song>,
    onlineSongs: List<JioSaavnSongItem> = emptyList(),
    isSearchingOnline: Boolean = false,
    resolvingSongId: String? = null,
    likedSongIds: Set<String> = emptySet(),
    currentSong: Song?,
    isPlaying: Boolean,
    userName: String = "User",
    isUpdateAvailable: Boolean = false,
    onOpenEditProfileDialog: () -> Unit = {},
    onSongClick: (Song) -> Unit,
    onOnlineSongClick: (JioSaavnSongItem) -> Unit = {},
    onToggleLikeOnline: (JioSaavnSongItem) -> Unit = {},
    onShareSong: ((Song) -> Unit)? = null,
    onShareOnlineSong: ((JioSaavnSongItem) -> Unit)? = null,
    onAddToPlaylistClick: (Song) -> Unit,
    onDeleteSong: (Song) -> Unit,
    selectedTab: HomeTab = HomeTab.SEARCH,
    onTabSelected: ((HomeTab) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val filterTabs = remember { listOf("All", "Online", "Device", "Tracks", "Artists") }

    // Top Result: Prefer local match if present, else first online match
    val topLocalResult = remember(filteredSongs, searchQuery) {
        if (searchQuery.isNotBlank() && filteredSongs.isNotEmpty()) filteredSongs.first() else null
    }
    val topOnlineResult = remember(onlineSongs, searchQuery) {
        if (searchQuery.isNotBlank() && onlineSongs.isNotEmpty()) onlineSongs.first() else null
    }

    val showOnline = selectedFilterTag in listOf("All", "Online")
    val showDevice = selectedFilterTag in listOf("All", "Device", "Tracks", "Artists")

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
                            text = "Search tracks, artists, streaming catalog...",
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSearchingOnline) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = NoctraLime,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
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
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                            if (searchQuery.isNotBlank()) onPerformOnlineSearch(searchQuery)
                        }
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

            // Filter Chips: All, Online, Device, Tracks, Artists
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

            // Top Result Card (if searching and has local or online result)
            if (topLocalResult != null) {
                item(key = "top_local_result") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(NoctraSurface)
                            .clickable { onSongClick(topLocalResult) }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E2330))
                            ) {
                                SongCoverImage(
                                    song = topLocalResult,
                                    modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "TOP MATCH • DEVICE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NoctraLime,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = topLocalResult.title,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NoctraTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${topLocalResult.artist} • Track",
                                    fontSize = 12.sp,
                                    color = NoctraTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(NoctraLime)
                                    .clickable { onSongClick(topLocalResult) },
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
            } else if (topOnlineResult != null) {
                item(key = "top_online_result") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(NoctraSurface)
                            .clickable { onOnlineSongClick(topOnlineResult) }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1E2330))
                            ) {
                                if (topOnlineResult.hdCoverUrl != null) {
                                    AsyncImage(
                                        model = topOnlineResult.hdCoverUrl,
                                        contentDescription = topOnlineResult.cleanTitle,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "TOP MATCH • CLOUD STREAM",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NoctraLime,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = topOnlineResult.cleanTitle,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NoctraTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${topOnlineResult.cleanArtist} • 320 KBPS",
                                    fontSize = 12.sp,
                                    color = NoctraTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(NoctraLime)
                                    .clickable { onOnlineSongClick(topOnlineResult) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play Online",
                                    tint = Color(0xFF0C0E12),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Online Streaming Results Section
            if (showOnline && onlineSongs.isNotEmpty()) {
                item(key = "online_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Online Streaming",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = NoctraTextPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NoctraLime.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "320K",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NoctraLime
                                )
                            }
                        }

                        Text(
                            text = "${onlineSongs.size} tracks",
                            fontSize = 13.sp,
                            color = NoctraTextMuted
                        )
                    }
                }

                itemsIndexed(
                    items = onlineSongs,
                    key = { _, item -> "online_${item.id ?: item.cleanTitle}" },
                    contentType = { _, _ -> "online_song" }
                ) { _, item ->
                    val isSelected = currentSong?.title == item.cleanTitle
                    val songId = item.id ?: item.cleanTitle.hashCode().toString()
                    val isLiked = likedSongIds.contains(songId)

                    NoctraSearchOnlineSongItem(
                        item = item,
                        isSelected = isSelected,
                        isPlaying = isSelected && isPlaying,
                        isResolving = resolvingSongId == item.id,
                        isLiked = isLiked,
                        onClick = { onOnlineSongClick(item) },
                        onToggleLike = { onToggleLikeOnline(item) },
                        onShare = { onShareOnlineSong?.invoke(item) },
                        onAddToPlaylist = {
                            val song = Song(
                                id = songId,
                                title = item.cleanTitle,
                                artist = item.cleanArtist,
                                audioUrl = item.encryptedMediaUrl ?: "",
                                coverUrl = item.hdCoverUrl,
                                durationMs = item.durationMs,
                                isLocal = false,
                                addedBy = "Online Stream"
                            )
                            onAddToPlaylistClick(song)
                        }
                    )
                }

                item(key = "online_divider") {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Local Device Storage Results Section
            if (showDevice && filteredSongs.isNotEmpty()) {
                item(key = "device_header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Device Storage",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NoctraTextPrimary
                        )

                        Text(
                            text = "${filteredSongs.size} files",
                            fontSize = 13.sp,
                            color = NoctraTextMuted
                        )
                    }
                }

                itemsIndexed(
                    items = filteredSongs,
                    key = { _, song -> "local_${song.id}" },
                    contentType = { _, _ -> "device_song" }
                ) { _, song ->
                    val isSelected = song.id == currentSong?.id
                    NoctraSearchSongItem(
                        song = song,
                        isSelected = isSelected,
                        isPlaying = isSelected && isPlaying,
                        onClick = { onSongClick(song) },
                        onShare = { onShareSong?.invoke(song) },
                        onAddToPlaylist = { onAddToPlaylistClick(song) },
                        onDelete = { onDeleteSong(song) }
                    )
                }
            }

            // Empty State
            if (filteredSongs.isEmpty() && onlineSongs.isEmpty() && !isSearchingOnline) {
                item(key = "empty_state") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (searchQuery.isBlank()) "Search any music" else "No matches found for '$searchQuery'",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NoctraTextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Search across 30M+ songs online & your device files",
                                fontSize = 13.sp,
                                color = NoctraTextMuted
                            )
                        }
                    }
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
                        text = "Searching streaming catalog + your device",
                        fontSize = 12.sp,
                        color = NoctraTextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun NoctraSearchOnlineSongItem(
    item: JioSaavnSongItem,
    isSelected: Boolean,
    isPlaying: Boolean,
    isResolving: Boolean,
    isLiked: Boolean,
    onClick: () -> Unit,
    onToggleLike: () -> Unit,
    onShare: (() -> Unit)? = null,
    onAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            if (item.hdCoverUrl != null) {
                AsyncImage(
                    model = item.hdCoverUrl,
                    contentDescription = item.cleanTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            if (isResolving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x99000000)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = NoctraLime,
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title & Artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.cleanTitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) NoctraLime else NoctraTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Podcasts,
                    contentDescription = null,
                    tint = NoctraLime,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${item.cleanArtist} • Online 320k",
                    fontSize = 12.sp,
                    color = NoctraTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Like Heart Button
        IconButton(
            onClick = onToggleLike,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isLiked) "Unlike" else "Like",
                tint = if (isLiked) NoctraLime else NoctraTextMuted,
                modifier = Modifier.size(19.dp)
            )
        }

        // Share Button
        if (onShare != null) {
            IconButton(
                onClick = onShare,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = NoctraTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Add to Playlist Button
        IconButton(
            onClick = onAddToPlaylist,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                contentDescription = "Add to playlist",
                tint = NoctraTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun NoctraSearchSongItem(
    song: Song,
    isSelected: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onShare: (() -> Unit)? = null,
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
                if (onShare != null) {
                    DropdownMenuItem(
                        text = { Text("Share Track", color = NoctraTextPrimary) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = null,
                                tint = NoctraLime
                            )
                        },
                        onClick = {
                            showMenu = false
                            onShare()
                        }
                    )
                }
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
