package com.example.basgaane.presentation.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import coil.compose.AsyncImage
import com.example.basgaane.domain.model.Song
import com.example.basgaane.presentation.components.EqualizerBars
import com.example.basgaane.presentation.components.crystalGlassCard
import com.example.basgaane.presentation.components.crystalPill
import com.example.basgaane.presentation.components.formatMs
import com.example.basgaane.presentation.theme.AccentGradientEnd
import com.example.basgaane.presentation.theme.AccentGradientStart
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
    modifier: Modifier = Modifier
) {
    val filterTags = listOf("All", "Cloud", "Device", "Synth", "Groove", "Chill", "Acoustic")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

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
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text("What do you want to listen to?", color = TextTertiary) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = PrismCyan
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = TextSecondary
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = Color(0x99171D2F),
                unfocusedContainerColor = Color(0x660E121E),
                focusedBorderColor = PrismCyan,
                unfocusedBorderColor = Color(0x33FFFFFF)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Liquid Crystal Filter Tags Horizontal Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterTags.forEach { tag ->
                val isSelected = tag.equals(selectedFilterTag, ignoreCase = true)

                Box(
                    modifier = Modifier
                        .crystalPill(
                            shape = RoundedCornerShape(20.dp),
                            isActive = isSelected,
                            onClick = { onFilterTagSelected(tag) }
                        )
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results Section
        if (filteredSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "No tracks matching '$searchQuery'" else "No songs found for this filter",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try searching for a different song name or artist",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
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
                itemsIndexed(filteredSongs, key = { _, song -> song.id }) { index, song ->
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

@Composable
private fun SearchSongItemCard(
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
            }

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
                            Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null, tint = NeonCyan)
                        },
                        onClick = {
                            showMenu = false
                            onAddToPlaylist()
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Delete Track", color = NeonPink) },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = NeonPink)
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
