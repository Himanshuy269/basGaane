package com.example.basgaane.ui.playlistdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.basgaane.domain.model.Playlist
import com.example.basgaane.domain.model.Song
import com.example.basgaane.ui.common.LiquidCrystalBackground

/**
 * Main Playlist Detail Screen displaying the tracks within a playlist, header stats,
 * and playback control triggers.
 */
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

    val (songCount, totalDurationMs) = remember(playlistSongs) {
        calculatePlaylistStats(playlistSongs)
    }

    val isLikedPlaylist = playlist.id == "liked_songs_playlist" || playlist.name.equals("Liked Songs", ignoreCase = true)

    LiquidCrystalBackground(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Top Navigation Bar
            PlaylistDetailTopBar(
                onBack = onBack,
                onDeletePlaylist = onDeletePlaylist,
                canDelete = !isLikedPlaylist
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Header Card with Playlist artwork
            PlaylistDetailHeaderCard(
                playlist = playlist,
                songCount = songCount,
                totalDurationMs = totalDurationMs
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Play All & Shuffle Buttons
            if (playlistSongs.isNotEmpty()) {
                PlaylistDetailActionsRow(
                    onPlayAll = { onPlayAll(playlistSongs) },
                    onShufflePlay = { onShufflePlay(playlistSongs) }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Track List or Empty State
            if (playlistSongs.isEmpty()) {
                PlaylistDetailEmptyState(
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    itemsIndexed(
                        items = playlistSongs,
                        key = { _, s -> s.id },
                        contentType = { _, _ -> "playlist_song_item" }
                    ) { index, song ->
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
