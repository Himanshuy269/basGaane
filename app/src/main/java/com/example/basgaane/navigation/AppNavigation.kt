package com.example.basgaane.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import com.example.basgaane.core.util.ShareUtils
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.basgaane.viewmodel.HomeTab
import com.example.basgaane.viewmodel.MusicViewModel
import com.example.basgaane.ui.common.AddMusicDialog
import com.example.basgaane.ui.common.AddToPlaylistDialog
import com.example.basgaane.ui.common.AppBottomNavigationBar
import com.example.basgaane.ui.common.AppUpdateSectionDialog
import com.example.basgaane.ui.common.CreatePlaylistDialog
import com.example.basgaane.ui.common.EditProfileDialog
import com.example.basgaane.ui.common.UpdateDialog
import com.example.basgaane.ui.home.HomeScreen
import com.example.basgaane.ui.online.OnlineMusicScreen
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
    val context = LocalContext.current

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

    val onlineSearchQuery by viewModel.onlineSearchQuery.collectAsState()
    val onlineSearchResults by viewModel.onlineSearchResults.collectAsState()
    val isSearchingOnline by viewModel.isSearchingOnline.collectAsState()
    val resolvingOnlineSongId by viewModel.resolvingOnlineSongId.collectAsState()
    val selectedOnlineGenre by viewModel.selectedOnlineGenre.collectAsState()
    val likedSongIds by viewModel.likedSongIds.collectAsState()
    val isSaveOnlinePlaylistDialogOpen by viewModel.isSaveOnlinePlaylistDialogOpen.collectAsState()

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
                NavigationRoutes.ONLINE -> viewModel.selectTab(HomeTab.ONLINE)
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
            HomeTab.ONLINE -> {
                navController.navigate(NavigationRoutes.ONLINE) {
                    popUpTo(NavigationRoutes.HOME)
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
                    onShareSong = { song -> ShareUtils.shareSong(context, song) },
                    onAddToPlaylistClick = viewModel::openAddToPlaylistDialog,
                    onDeleteSong = viewModel::deleteSong,
                    onAddMusicClick = viewModel::openAddMusicDialog,
                    onOpenEditProfileDialog = viewModel::openEditProfileDialog,
                    updateInfo = updateInfo,
                    onOpenAppUpdateDialog = viewModel::openAppUpdateDialog,
                    snackbarHostState = snackbarHostState
                )
            }

            // Online Music Streaming Destination
            composable(NavigationRoutes.ONLINE) {
                OnlineMusicScreen(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected,
                    searchQuery = onlineSearchQuery,
                    onSearchQueryChanged = viewModel::onOnlineSearchQueryChanged,
                    onPerformSearch = viewModel::performOnlineSearch,
                    selectedGenre = selectedOnlineGenre,
                    onSelectGenre = viewModel::selectOnlineGenre,
                    onlineSongs = onlineSearchResults,
                    likedSongIds = likedSongIds,
                    userName = userName,
                    isUpdateAvailable = updateInfo != null,
                    onOpenEditProfileDialog = viewModel::openEditProfileDialog,
                    onToggleLike = viewModel::toggleLikeOnlineSong,
                    isSearching = isSearchingOnline,
                    resolvingSongId = resolvingOnlineSongId,
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    onSongClick = viewModel::playOnlineSong,
                    onShareSong = { item -> ShareUtils.shareOnlineItem(context, item) },
                    onAddToPlaylistClick = viewModel::openAddToPlaylistDialog,
                    onCreatePlaylistClick = viewModel::openCreatePlaylistDialog,
                    onSaveResultsAsPlaylist = viewModel::openSaveOnlinePlaylistDialog
                )
            }

            // Search Destination
            composable(NavigationRoutes.SEARCH) {
                SearchScreen(
                    searchQuery = searchQuery,
                    selectedFilterTag = searchFilterTag,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onFilterTagSelected = viewModel::onSearchFilterTagChanged,
                    onPerformOnlineSearch = viewModel::performOnlineSearch,
                    filteredSongs = filteredSongs,
                    onlineSongs = onlineSearchResults,
                    isSearchingOnline = isSearchingOnline,
                    resolvingSongId = resolvingOnlineSongId,
                    likedSongIds = likedSongIds,
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    userName = userName,
                    isUpdateAvailable = updateInfo != null,
                    onOpenEditProfileDialog = viewModel::openEditProfileDialog,
                    onSongClick = viewModel::playSong,
                    onOnlineSongClick = viewModel::playOnlineSong,
                    onToggleLikeOnline = viewModel::toggleLikeOnlineSong,
                    onShareSong = { song -> ShareUtils.shareSong(context, song) },
                    onShareOnlineSong = { item -> ShareUtils.shareOnlineItem(context, item) },
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
                    onPlayPlaylist = { playlist ->
                        val targetSongs = allSongs.filter { s -> playlist.songIds.contains(s.id) }
                        if (targetSongs.isNotEmpty()) {
                            viewModel.playPlaylist(targetSongs, null)
                        }
                    },
                    userName = userName,
                    isUpdateAvailable = updateInfo != null,
                    onOpenEditProfileDialog = viewModel::openEditProfileDialog,
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
                        onShareSong = { song -> ShareUtils.shareSong(context, song) },
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

        // Persistent Dock: MiniPlayer + Bottom Navigation Bar
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        val isTopLevelDestination = currentRoute in listOf(
            NavigationRoutes.HOME,
            NavigationRoutes.ONLINE,
            NavigationRoutes.SEARCH,
            NavigationRoutes.PLAYLISTS,
            null
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
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

            if (isTopLevelDestination) {
                AppBottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected
                )
            }
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

    // Modal: Save Online Results as Playlist Dialog
    CreatePlaylistDialog(
        isOpen = isSaveOnlinePlaylistDialogOpen,
        initialName = "${selectedOnlineGenre.replaceFirstChar { it.uppercase() }} Stream",
        titleText = "Save Online Tracks as Playlist",
        onDismiss = viewModel::closeSaveOnlinePlaylistDialog,
        onCreate = { name, desc ->
            viewModel.saveOnlineResultsAsPlaylist(name, desc)
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
            isLiked = playerState.currentSong?.id?.let { likedSongIds.contains(it) } == true,
            onToggleLike = { playerState.currentSong?.let { viewModel.toggleLikeSong(it) } },
            onShare = { playerState.currentSong?.let { ShareUtils.shareSong(context, it) } },
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
