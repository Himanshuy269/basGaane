package com.example.basgaane.ui.online

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.basgaane.ui.theme.NoctraBorder
import com.example.basgaane.ui.theme.NoctraChipInactive
import com.example.basgaane.ui.theme.NoctraDark
import com.example.basgaane.ui.theme.NoctraLime
import com.example.basgaane.ui.theme.NoctraSurface
import com.example.basgaane.ui.theme.NoctraSurfaceElevated
import com.example.basgaane.ui.theme.NoctraTextMuted
import com.example.basgaane.ui.theme.NoctraTextPrimary
import com.example.basgaane.ui.theme.NoctraTextSecondary
import com.example.basgaane.viewmodel.HomeTab
import java.util.Locale

/**
 * Screen 2: Online Stream Screen ("Stay curious.")
 * Matches Screenshot 3 in the Noctra design system.
 */
@Composable
fun OnlineMusicScreen(
    selectedTab: HomeTab = HomeTab.ONLINE,
    onTabSelected: ((HomeTab) -> Unit)? = null,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onPerformSearch: (String) -> Unit,
    selectedGenre: String,
    onSelectGenre: (String) -> Unit,
    onlineSongs: List<JioSaavnSongItem>,
    isSearching: Boolean,
    resolvingSongId: String?,
    currentSong: Song?,
    isPlaying: Boolean,
    likedSongIds: Set<String> = emptySet(),
    userName: String = "User",
    isUpdateAvailable: Boolean = false,
    onOpenEditProfileDialog: () -> Unit = {},
    onSongClick: (JioSaavnSongItem) -> Unit,
    onToggleLike: (JioSaavnSongItem) -> Unit = {},
    onShareSong: ((JioSaavnSongItem) -> Unit)? = null,
    onAddToPlaylistClick: (Song) -> Unit,
    onCreatePlaylistClick: () -> Unit = {},
    onSaveResultsAsPlaylist: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val quickGenres = remember {
        listOf(
            "Trending",
            "Bollywood Hits",
            "Global Top 50",
            "Punjabi Beats",
            "Arijit Singh",
            "Imagine Dragons",
            "Lofi & Chill",
            "EDM & Bass",
            "Sidhu Moose Wala",
            "Taylor Swift",
            "K-Pop Hits"
        )
    }

    val featuredSong = onlineSongs.firstOrNull()

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
                    text = "A WORLD BEYOND YOUR LIBRARY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NoctraTextMuted,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Stay curious.",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NoctraTextPrimary
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Featured Hero Card ("The night shift.")
            item(key = "hero_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(NoctraSurface)
                        .clickable {
                            featuredSong?.let { onSongClick(it) }
                        }
                ) {
                    // Background Cover Image with Gradient
                    if (featuredSong?.hdCoverUrl != null) {
                        AsyncImage(
                            model = featuredSong.hdCoverUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Dark overlay gradient for crisp typography
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x880C0E12),
                                        Color(0xD90C0E12),
                                        Color(0xF50C0E12)
                                    )
                                )
                            )
                    )

                    // Card Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top label with green dot
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NoctraLime)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BASGAANE SELECTS / 032",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NoctraTextPrimary,
                                letterSpacing = 1.sp
                            )
                        }

                        // Title, Subtitle & Circular Play FAB
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = featuredSong?.cleanTitle ?: "The night shift.",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NoctraTextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 32.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = featuredSong?.cleanArtist ?: "Left-field sounds. No curfew.",
                                    fontSize = 13.sp,
                                    color = NoctraTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Circular Lime Green Play FAB
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(NoctraLime)
                                    .clickable {
                                        featuredSong?.let { onSongClick(it) }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color(0xFF0C0E12),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Audio Quality & Track Count Pill Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Podcasts,
                            contentDescription = null,
                            tint = NoctraLime,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LOSSLESS AUDIO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NoctraTextMuted,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Text(
                        text = "${onlineSongs.size} tracks • 320 KBPS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = NoctraTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Search Bar Input
            item(key = "search_input") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    placeholder = {
                        Text(
                            text = "Search tracks, artists, global catalog...",
                            fontSize = 14.sp,
                            color = NoctraTextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = NoctraTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = NoctraLime,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
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
                            if (searchQuery.isNotBlank()) onPerformSearch(searchQuery)
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

            // Quick Genre Chips
            item(key = "genre_chips") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickGenres.forEach { genre ->
                        val isSelected = selectedGenre.equals(genre, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) NoctraLime else NoctraChipInactive)
                                .clickable { onSelectGenre(genre) }
                                .padding(horizontal = 16.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = genre,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF0C0E12) else Color(0xFFC5CBD8)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Section: "In your orbit"
            item(key = "in_your_orbit") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "In your orbit",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NoctraTextPrimary
                    )

                    Text(
                        text = "View all",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = NoctraTextMuted,
                        modifier = Modifier.clickable { onSaveResultsAsPlaylist() }
                    )
                }

                // Curated Two-Card Row (Soft frequencies & Velvet afterglow)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 1
                    CuratedOrbitCard(
                        title = "Soft frequencies",
                        subtitle = "Ambient, downtempo & beyond",
                        onClick = { onSelectGenre("Lofi & Chill") },
                        modifier = Modifier.weight(1f)
                    )

                    // Card 2
                    CuratedOrbitCard(
                        title = "Velvet afterglow",
                        subtitle = "Soul for the small hours",
                        onClick = { onSelectGenre("Bollywood Hits") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Underground Live Radio Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(NoctraSurface)
                        .clickable {
                            if (onlineSongs.isNotEmpty()) onSongClick(onlineSongs.first())
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF1E2812)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Podcasts,
                                    contentDescription = null,
                                    tint = NoctraLime,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Underground, on air",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NoctraTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Live radio • Electronic",
                                    fontSize = 12.sp,
                                    color = NoctraTextSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open",
                            tint = NoctraTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Stream Track Results Section
            if (isSearching) {
                item(key = "loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NoctraLime, modifier = Modifier.size(36.dp))
                    }
                }
            } else if (onlineSongs.isEmpty()) {
                item(key = "empty_online") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (searchQuery.isBlank()) "No online tracks found" else "No results found for '$searchQuery'",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NoctraTextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try searching for another song, artist, or genre",
                                fontSize = 13.sp,
                                color = NoctraTextMuted
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = onlineSongs,
                    key = { _, song -> song.id ?: song.cleanTitle },
                    contentType = { _, _ -> "online_song" }
                ) { _, songItem ->
                    val isResolving = songItem.id == resolvingSongId
                    val isCurrent = currentSong?.title.equals(songItem.cleanTitle, ignoreCase = true)
                    val isLiked = songItem.id?.let { likedSongIds.contains(it) } == true

                    NoctraOnlineSongListItem(
                        songItem = songItem,
                        isCurrentPlaying = isCurrent && isPlaying,
                        isResolving = isResolving,
                        isLiked = isLiked,
                        onSongClick = { onSongClick(songItem) },
                        onToggleLike = { onToggleLike(songItem) },
                        onShare = { onShareSong?.invoke(songItem) },
                        onAddToPlaylist = {
                            val domainSong = Song(
                                id = songItem.id ?: songItem.cleanTitle.hashCode().toString(),
                                title = songItem.cleanTitle,
                                artist = songItem.cleanArtist,
                                audioUrl = songItem.encryptedMediaUrl ?: "",
                                coverUrl = songItem.hdCoverUrl,
                                durationMs = songItem.durationMs,
                                isLocal = false,
                                addedBy = "Online Stream"
                            )
                            onAddToPlaylistClick(domainSong)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CuratedOrbitCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(NoctraSurface)
            .clickable { onClick() }
            .padding(14.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Column {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = NoctraTextPrimary
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = NoctraTextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Clean Online Stream Track List Item matching the dark & lime styling.
 */
@Composable
fun NoctraOnlineSongListItem(
    songItem: JioSaavnSongItem,
    isCurrentPlaying: Boolean,
    isResolving: Boolean,
    isLiked: Boolean,
    onSongClick: () -> Unit,
    onToggleLike: () -> Unit,
    onShare: (() -> Unit)? = null,
    onAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onSongClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // HD Cover Image
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1E2330))
        ) {
            if (!songItem.hdCoverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = songItem.hdCoverUrl,
                    contentDescription = songItem.cleanTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title & Artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = songItem.cleanTitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isCurrentPlaying) NoctraLime else NoctraTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = songItem.cleanArtist,
                fontSize = 12.sp,
                color = NoctraTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Resolving spinner / Equalizer or duration
        if (isResolving) {
            CircularProgressIndicator(
                color = NoctraLime,
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else if (isCurrentPlaying) {
            EqualizerBars(
                isPlaying = true,
                barColor = NoctraLime,
                modifier = Modifier.padding(end = 4.dp)
            )
        } else if (songItem.durationMs > 0) {
            Text(
                text = formatDurationOnline(songItem.durationMs),
                fontSize = 12.sp,
                color = NoctraTextMuted
            )
        }

        // Heart Like Button
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

        // 3-dot Add to Playlist Button
        IconButton(
            onClick = onAddToPlaylist,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                contentDescription = "Add to Playlist",
                tint = NoctraTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun formatDurationOnline(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
