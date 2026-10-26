package com.example.basgaane.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.basgaane.viewmodel.HomeTab
import com.example.basgaane.viewmodel.MusicViewModel
import com.example.basgaane.ui.common.AddMusicDialog
import com.example.basgaane.ui.common.AddToPlaylistDialog
import com.example.basgaane.ui.common.AppUpdateSectionDialog
import com.example.basgaane.ui.common.CreatePlaylistDialog
import com.example.basgaane.ui.common.EditProfileDialog
import com.example.basgaane.ui.common.UpdateDialog
import com.example.basgaane.ui.home.HomeScreen
import com.example.basgaane.ui.player.FullPlayerSheet
import com.example.basgaane.ui.player.MiniPlayer
import com.example.basgaane.ui.playlistdetail.PlaylistDetailScreen
import com.example.basgaane.ui.playlists.PlaylistsScreen
import com.example.basgaane.ui.search.SearchScreen
import com.example.basgaane.ui.theme.NeonCyan
import com.example.basgaane.ui.theme.SurfaceDarkVariant
import com.example.basgaane.ui.theme.TextPrimary

/**
 * Central navigation controller and host for the BasGaane application.
 * Manages screen transitions, persistent mini-player, modal dialogs, and full-screen player.
 */
@Composable
fun AppNavigation(
    viewModel: MusicViewModel = viewModel()
) {
    val navController = rememberNavController()

    val allSongs by viewModel.allSongs.collectAsState()
    val filteredSongs by viewModel.filteredSongs.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchFilterTag by viewModel.searchFilterTag.collectAsState()
    val playlists by viewModel.playlists.collectAsState()

    val currentSong by viewModel.currentPlayingSong.collectAsState()
    val isPlaying by viewModel.isTrackPlaying.collectAsState()
    val playerState by viewModel.playerUiState.collectAsState()
    val isAddDialogOpen by viewModel.isAddMusicDialogOpen.collectAsState()
    val isCreatePlaylistDialogOpen by viewModel.isCreatePlaylistDialogOpen.collectAsState()
    val songForAddToPlaylist by viewModel.songForAddToPlaylist.collectAsState()
    val isFullPlayerVisible by viewModel.isFullPlayerVisible.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val isEditProfileDialogOpen by viewModel.isEditProfileDialogOpen.collectAsState()
    val isAppUpdateDialogOpen by viewModel.isAppUpdateDialogOpen.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    val updateInfo by viewModel.updateInfo.collectAsState()
    val isAutoUpdatePopupVisible by viewModel.isAutoUpdatePopupVisible.collectAsState()
    val downloadState by viewModel.downloadState.collectAsState()
    val isCheckingUpdates by viewModel.isCheckingForUpdates.collectAsState()
    val updateFeedback by viewModel.updateCheckFeedback.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Check for updates in the background on app start
    LaunchedEffect(Unit) {
        viewModel.checkForUpdates(manual = false)
    }

    LaunchedEffect(userMessage) {
        userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearUserMessage()
        }
    }

    // Sync tab selection state when back stack changes
    LaunchedEffect(navController) {
        navController.currentBackStackEntryFlow.collect { backStackEntry ->
            when (backStackEntry.destination.route) {
                NavigationRoutes.HOME -> viewModel.selectTab(HomeTab.HOME)
                NavigationRoutes.SEARCH -> viewModel.selectTab(HomeTab.SEARCH)
                NavigationRoutes.PLAYLISTS -> viewModel.selectTab(HomeTab.PLAYLISTS)
            }
        }
    }

    val onTabSelected: (HomeTab) -> Unit = { tab ->
        viewModel.selectTab(tab)
        when (tab) {
            HomeTab.HOME -> {
                navController.navigate(NavigationRoutes.HOME) {
                    popUpTo(NavigationRoutes.HOME) { inclusive = true }
                    launchSingleTop = true
                }
            }
            HomeTab.SEARCH -> {
                navController.navigate(NavigationRoutes.SEARCH) {
                    popUpTo(NavigationRoutes.HOME)
                    launchSingleTop = true
                }
            }
            HomeTab.PLAYLISTS -> {
                navController.navigate(NavigationRoutes.PLAYLISTS) {
                    popUpTo(NavigationRoutes.HOME)
                    launchSingleTop = true
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = NavigationRoutes.HOME,
            modifier = Modifier.fillMaxSize()
        ) {
            // Home / All Tracks Destination
            composable(NavigationRoutes.HOME) {
                HomeScreen(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected,
                    songs = allSongs,
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    userName = userName,
                    themeMode = themeMode,
                    onToggleTheme = viewModel::toggleTheme,
                    onSongClick = viewModel::playSong,
                    onAddToPlaylistClick = viewModel::openAddToPlaylistDialog,
                    onDeleteSong = viewModel::deleteSong,
                    onAddMusicClick = viewModel::openAddMusicDialog,
                    onOpenEditProfileDialog = viewModel::openEditProfileDialog,
                    updateInfo = updateInfo,
                    onOpenAppUpdateDialog = viewModel::openAppUpdateDialog,
                    snackbarHostState = snackbarHostState
                )
            }

            // Search Destination
            composable(NavigationRoutes.SEARCH) {
                SearchScreen(
                    searchQuery = searchQuery,
                    selectedFilterTag = searchFilterTag,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onFilterTagSelected = viewModel::onSearchFilterTagChanged,
                    filteredSongs = filteredSongs,
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    onSongClick = viewModel::playSong,
                    onAddToPlaylistClick = viewModel::openAddToPlaylistDialog,
                    onDeleteSong = viewModel::deleteSong,
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected
                )
            }

            // Playlists List Destination
            composable(NavigationRoutes.PLAYLISTS) {
                PlaylistsScreen(
                    playlists = playlists,
                    onPlaylistClick = { playlist ->
                        navController.navigate(NavigationRoutes.playlistDetail(playlist.id))
                    },
                    onCreatePlaylistClick = viewModel::openCreatePlaylistDialog,
                    onDeletePlaylist = viewModel::deletePlaylist,
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected
                )
            }

            // Playlist Detail Destination
            composable(
                route = NavigationRoutes.PLAYLIST_DETAIL,
                arguments = listOf(navArgument("playlistId") { type = NavType.StringType })
            ) { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getString("playlistId").orEmpty()
                val playlist = playlists.find { it.id == playlistId }

                if (playlist != null) {
                    PlaylistDetailScreen(
                        playlist = playlist,
                        allSongs = allSongs,
                        currentSong = currentSong,
                        isPlaying = isPlaying,
                        onBack = { navController.popBackStack() },
                        onPlayAll = { songs -> viewModel.playPlaylist(songs, null) },
                        onShufflePlay = { songs -> viewModel.playPlaylist(songs.shuffled(), null) },
                        onSongClick = { targetSong, songs -> viewModel.playPlaylist(songs, targetSong) },
                        onRemoveSongFromPlaylist = { songId ->
                            viewModel.removeSongFromPlaylist(playlist.id, songId)
                        },
                        onDeletePlaylist = {
                            viewModel.deletePlaylist(playlist)
                            navController.popBackStack()
                        }
                    )
                }
            }
        }

        // Persistent Docked MiniPlayer
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        ) {
            MiniPlayer(
                currentSong = playerState.currentSong,
                isPlaying = playerState.isPlaying,
                progressMs = playerState.currentPositionMs,
                durationMs = playerState.durationMs,
                onPlayPause = viewModel::togglePlayPause,
                onSkipNext = viewModel::skipNext,
                onClick = viewModel::showFullPlayer
            )
        }

        // Global Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = SurfaceDarkVariant,
                contentColor = TextPrimary,
                actionColor = NeonCyan,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }

    // Modal: Add Music Dialog
    AddMusicDialog(
        isOpen = isAddDialogOpen,
        isUploading = isUploading,
        currentUserName = userName,
        onDismiss = viewModel::closeAddMusicDialog,
        onAddTrack = { title, artist, uri, coverBytes, coverUri, addedBy ->
            viewModel.addTrack(
                title = title,
                artist = artist,
                audioUri = uri,
                coverBytes = coverBytes,
                coverUri = coverUri,
                addedBy = addedBy
            )
        }
    )

    // Modal: Edit Profile Dialog
    EditProfileDialog(
        isOpen = isEditProfileDialogOpen,
        currentName = userName,
        currentThemeMode = themeMode,
        onSelectThemeMode = viewModel::setThemeMode,
        onDismiss = viewModel::closeEditProfileDialog,
        onSaveName = viewModel::setUserName,
        appVersion = viewModel.currentVersionName,
        updateInfo = updateInfo,
        isCheckingForUpdates = isCheckingUpdates,
        downloadState = downloadState,
        onCheckForUpdates = { viewModel.checkForUpdates(manual = true) },
        onStartDownload = viewModel::startUpdateDownload,
        onInstall = viewModel::installUpdate
    )

    // Modal: Dedicated App Updates Section Dialog
    AppUpdateSectionDialog(
        isOpen = isAppUpdateDialogOpen,
        currentVersionName = viewModel.currentVersionName,
        updateInfo = updateInfo,
        isCheckingForUpdates = isCheckingUpdates,
        downloadState = downloadState,
        onCheckForUpdates = { viewModel.checkForUpdates(manual = true) },
        onStartDownload = viewModel::startUpdateDownload,
        onInstall = viewModel::installUpdate,
        onDismiss = viewModel::closeAppUpdateDialog
    )

    // Modal: In-App Update Dialog (Full popup)
    if (isAutoUpdatePopupVisible) {
        UpdateDialog(
            updateInfo = updateInfo,
            currentVersionName = viewModel.currentVersionName,
            downloadState = downloadState,
            onStartDownload = viewModel::startUpdateDownload,
            onInstall = viewModel::installUpdate,
            onDismiss = viewModel::dismissUpdateDialog
        )
    }

    // Modal: Create Playlist Dialog
    CreatePlaylistDialog(
        isOpen = isCreatePlaylistDialogOpen,
        onDismiss = viewModel::closeCreatePlaylistDialog,
        onCreate = { name, desc ->
            viewModel.createPlaylist(name, desc)
        }
    )

    // Modal: Add Song To Playlist Dialog
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

    // Modal: Full-Screen Player Sheet
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
