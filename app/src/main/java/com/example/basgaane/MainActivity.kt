package com.example.basgaane

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.basgaane.presentation.components.AddMusicDialog
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
    // Request notification permission for Android 13+ media controls
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

    val songs by viewModel.filteredSongs.collectAsState()
    val playerState by viewModel.playerUiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isAddDialogOpen by viewModel.isAddMusicDialogOpen.collectAsState()
    val isFullPlayerVisible by viewModel.isFullPlayerVisible.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val uploadStatus by viewModel.uploadStatusMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uploadStatus) {
        uploadStatus?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearUploadStatusMessage()
        }
    }

    HomeScreen(
        songs = songs,
        currentSong = playerState.currentSong,
        isPlaying = playerState.isPlaying,
        progressMs = playerState.currentPositionMs,
        durationMs = playerState.durationMs,
        searchQuery = searchQuery,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
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