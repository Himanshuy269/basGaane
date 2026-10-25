package com.example.basgaane.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.basgaane.domain.model.Song
import com.example.basgaane.viewmodel.HomeTab
import com.example.basgaane.viewmodel.ThemeMode
import com.example.basgaane.ui.common.LiquidCrystalBackground
import com.example.basgaane.ui.common.crystalPill
import com.example.basgaane.ui.theme.BackgroundDark
import com.example.basgaane.ui.theme.NeonCyan
import com.example.basgaane.ui.theme.SurfaceDarkVariant
import com.example.basgaane.ui.theme.TextPrimary

/**
 * Main Home screen composable rendering the track library, liquid crystal top bar,
 * tab switcher capsule, and music addition trigger.
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
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    bottomBar: @Composable () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        containerColor = BackgroundDark,
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
            ExtendedFloatingActionButton(
                onClick = onAddMusicClick,
                containerColor = Color.Transparent,
                contentColor = TextPrimary,
                shape = RoundedCornerShape(20.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Music", tint = TextPrimary) },
                text = { Text("Add Music", fontWeight = FontWeight.Bold, color = TextPrimary) },
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = if (currentSong != null) 76.dp else 16.dp)
                    .crystalPill(shape = RoundedCornerShape(20.dp), isActive = true)
            )
        },
        bottomBar = bottomBar,
        modifier = modifier
    ) { innerPadding ->
        LiquidCrystalBackground(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                // Liquid Crystal Top Bar
                HomeTopBar(
                    songCount = songs.size,
                    userName = userName,
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme,
                    onOpenEditProfileDialog = onOpenEditProfileDialog
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Crystal Nav Tabs Capsule
                HomeTabsCapsule(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Song list with friend attribution and liquid crystal cards
                AllTracksContent(
                    songs = songs,
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    onSongClick = onSongClick,
                    onAddToPlaylist = onAddToPlaylistClick,
                    onDeleteSong = onDeleteSong,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
