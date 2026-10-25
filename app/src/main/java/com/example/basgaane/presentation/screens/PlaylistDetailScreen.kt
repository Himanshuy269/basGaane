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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    allSongs: List<Song>,
    currentSong: Song?,
    isPlaying: Boolean,
    onBack: () -> Unit,
    onPlayAll: (List<Song>) -> Unit,
    onShufflePlay: (List<Song>) -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    onRemoveSongFromPlaylist: (songId: String) -> Unit,
    onDeletePlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Resolve full songs in this playlist in order
    val playlistSongs = remember(playlist.songIds, allSongs) {
        val songMap = allSongs.associateBy { it.id }
        playlist.songIds.mapNotNull { songMap[it] }
    }

    val totalDurationMs = remember(playlistSongs) {
        playlistSongs.sumOf { it.durationMs }
    }

    var showPlaylistMenu by remember { mutableStateOf(false) }

    LiquidCrystalBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Top Navigation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .crystalPill(shape = CircleShape, isActive = false, onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "Playlist",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )

                Box {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .crystalPill(shape = CircleShape, isActive = false, onClick = { showPlaylistMenu = true }),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showPlaylistMenu,
                        onDismissRequest = { showPlaylistMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Playlist", color = NeonPink) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = NeonPink
                                )
                            },
                            onClick = {
                                showPlaylistMenu = false
                                onDeletePlaylist()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Header Card with Vinyl/Cover artwork
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .crystalGlassCard(shape = RoundedCornerShape(20.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(PrismViolet, PrismCyan)
                            )
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                listOf(PrismCyan, PrismMagenta)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (playlist.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = playlist.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${playlistSongs.size} tracks",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = PrismCyan
                        )

                        if (totalDurationMs > 0) {
                            Text(
                                text = " • ${formatMs(totalDurationMs)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Play All & Shuffle Buttons
            if (playlistSongs.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .crystalPill(
                                shape = RoundedCornerShape(16.dp),
                                isActive = true,
                                onClick = { onPlayAll(playlistSongs) }
                            )
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play All", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .crystalPill(
                                shape = RoundedCornerShape(16.dp),
                                isActive = false,
                                onClick = { onShufflePlay(playlistSongs) }
                            )
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shuffle, contentDescription = null, tint = PrismCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Shuffle", fontWeight = FontWeight.Bold, color = PrismCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Track List
            if (playlistSongs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "This playlist is empty",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Go to Home or Search and tap ⋮ to add songs!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextTertiary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    itemsIndexed(playlistSongs, key = { _, s -> s.id }) { index, song ->
                        val isCurrent = song.id == currentSong?.id

                        PlaylistItemCard(
                            index = index + 1,
                            song = song,
                            isSelected = isCurrent,
                            isPlaying = isCurrent && isPlaying,
                            onClick = { onSongClick(song, playlistSongs) },
                            onRemove = { onRemoveSongFromPlaylist(song.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaylistItemCard(
    index: Int,
    song: Song,
    isSelected: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
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
                    EqualizerBars(isPlaying = true, barColor = Color.White)
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

                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (song.durationMs > 0) {
                Text(
                    text = formatMs(song.durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RemoveCircleOutline,
                    contentDescription = "Remove from playlist",
                    tint = TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
