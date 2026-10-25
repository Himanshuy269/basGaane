package com.example.basgaane

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.basgaane.presentation.components.AddMusicDialog
import com.example.basgaane.presentation.components.AddToPlaylistDialog
import com.example.basgaane.presentation.components.CreatePlaylistDialog
import com.example.basgaane.presentation.components.FullPlayerSheet
import com.example.basgaane.presentation.screens.HomeScreen
import com.example.basgaane.presentation.theme.BackgroundDark
import com.example.basgaane.presentation.theme.BasGaaneTheme
import com.example.basgaane.presentation.viewmodel.MusicViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BasGaaneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    BasGaaneApp()
                }
            }
        }
    }
}

@Composable
fun BasGaaneApp(
    viewModel: MusicViewModel = viewModel()
) {
    // Request notification & media audio permissions for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    val allSongs by viewModel.allSongs.collectAsState()
    val filteredSongs by viewModel.filteredSongs.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchFilterTag by viewModel.searchFilterTag.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()

    val playerState by viewModel.playerUiState.collectAsState()
    val isAddDialogOpen by viewModel.isAddMusicDialogOpen.collectAsState()
    val isCreatePlaylistDialogOpen by viewModel.isCreatePlaylistDialogOpen.collectAsState()
    val songForAddToPlaylist by viewModel.songForAddToPlaylist.collectAsState()
    val isFullPlayerVisible by viewModel.isFullPlayerVisible.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Intercept back button when inspecting a playlist to return to playlists tab
    BackHandler(enabled = selectedPlaylist != null) {
        viewModel.selectPlaylist(null)
    }

    LaunchedEffect(userMessage) {
        userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearUserMessage()
        }
    }

    HomeScreen(
        selectedTab = selectedTab,
        onTabSelected = viewModel::selectTab,
        songs = allSongs,
        filteredSongs = filteredSongs,
        searchQuery = searchQuery,
        searchFilterTag = searchFilterTag,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onSearchFilterTagChanged = viewModel::onSearchFilterTagChanged,
        playlists = playlists,
        selectedPlaylist = selectedPlaylist,
        onSelectPlaylist = viewModel::selectPlaylist,
        onCreatePlaylistClick = viewModel::openCreatePlaylistDialog,
        onDeletePlaylist = viewModel::deletePlaylist,
        onAddToPlaylistClick = viewModel::openAddToPlaylistDialog,
        onRemoveSongFromPlaylist = viewModel::removeSongFromPlaylist,
        onPlayPlaylist = viewModel::playPlaylist,
        currentSong = playerState.currentSong,
        isPlaying = playerState.isPlaying,
        progressMs = playerState.currentPositionMs,
        durationMs = playerState.durationMs,
        onSongClick = viewModel::playSong,
        onPlayPause = viewModel::togglePlayPause,
        onSkipNext = viewModel::skipNext,
        onMiniPlayerClick = viewModel::showFullPlayer,
        onAddMusicClick = viewModel::openAddMusicDialog,
        onDeleteSong = viewModel::deleteSong,
        snackbarHostState = snackbarHostState
    )

    // Add Music Dialog Modal
    AddMusicDialog(
        isOpen = isAddDialogOpen,
        isUploading = isUploading,
        onDismiss = viewModel::closeAddMusicDialog,
        onAddTrack = { title, artist, uri ->
            viewModel.addTrack(
                title = title,
                artist = artist,
                audioUri = uri
            )
        }
    )

    // Create Playlist Dialog Modal
    CreatePlaylistDialog(
        isOpen = isCreatePlaylistDialogOpen,
        onDismiss = viewModel::closeCreatePlaylistDialog,
        onCreate = { name, desc ->
            viewModel.createPlaylist(name, desc)
        }
    )

    // Add To Playlist Dialog Modal
    AddToPlaylistDialog(
        song = songForAddToPlaylist,
        playlists = playlists,
        isOpen = songForAddToPlaylist != null,
        onDismiss = viewModel::closeAddToPlaylistDialog,
        onAddToPlaylist = { playlist ->
            songForAddToPlaylist?.let { song ->
                viewModel.addSongToPlaylist(playlist, song)
            }
        },
        onCreateNewPlaylistClick = viewModel::openCreatePlaylistDialog
    )

    // Full Screen Player Bottom Sheet
    if (isFullPlayerVisible) {
        FullPlayerSheet(
            song = playerState.currentSong,
            isPlaying = playerState.isPlaying,
            progressMs = playerState.currentPositionMs,
            durationMs = playerState.durationMs,
            isShuffle = playerState.isShuffleEnabled,
            repeatMode = playerState.repeatMode,
            onPlayPause = viewModel::togglePlayPause,
            onSeek = viewModel::seekTo,
            onSkipNext = viewModel::skipNext,
            onSkipPrevious = viewModel::skipPrevious,
            onToggleShuffle = viewModel::toggleShuffle,
            onToggleRepeat = viewModel::toggleRepeat,
            onDismiss = viewModel::hideFullPlayer
        )
    }
}