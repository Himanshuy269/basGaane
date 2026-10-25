package com.example.basgaane.viewmodel

import android.app.Application
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.basgaane.core.player.MusicPlayerController
import com.example.basgaane.core.player.PlayerUiState
import com.example.basgaane.core.utils.AudioMetadataHelper
import com.example.basgaane.data.repository.MusicRepositoryImpl
import com.example.basgaane.data.repository.PlaylistRepositoryImpl
import com.example.basgaane.domain.model.Playlist
import com.example.basgaane.domain.model.Song
import com.example.basgaane.domain.usecase.AddSongToPlaylistUseCase
import com.example.basgaane.domain.usecase.AddSongUseCase
import com.example.basgaane.domain.usecase.CreatePlaylistUseCase
import com.example.basgaane.domain.usecase.DeletePlaylistUseCase
import com.example.basgaane.domain.usecase.DeleteSongUseCase
import com.example.basgaane.domain.usecase.GetPlaylistsUseCase
import com.example.basgaane.domain.usecase.GetSongsUseCase
import com.example.basgaane.domain.usecase.RemoveSongFromPlaylistUseCase
import com.example.basgaane.domain.usecase.SearchSongsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class HomeTab {
    HOME,
    SEARCH,
    PLAYLISTS
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

/**
 * Presentation ViewModel coordinating domain Use Cases and exposing UI state.
 */
class MusicViewModel(
    application: Application,
    private val getSongsUseCase: GetSongsUseCase,
    private val addSongUseCase: AddSongUseCase,
    private val deleteSongUseCase: DeleteSongUseCase,
    private val searchSongsUseCase: SearchSongsUseCase,
    private val getPlaylistsUseCase: GetPlaylistsUseCase,
    private val createPlaylistUseCase: CreatePlaylistUseCase,
    private val deletePlaylistUseCase: DeletePlaylistUseCase,
    private val addSongToPlaylistUseCase: AddSongToPlaylistUseCase,
    private val removeSongFromPlaylistUseCase: RemoveSongFromPlaylistUseCase,
    private val playerController: MusicPlayerController
) : AndroidViewModel(application) {

    // Default factory constructor for ViewModelProvider
    constructor(application: Application) : this(
        application = application,
        musicRepository = MusicRepositoryImpl(application),
        playlistRepository = PlaylistRepositoryImpl(application)
    )

    private constructor(
        application: Application,
        musicRepository: MusicRepositoryImpl,
        playlistRepository: PlaylistRepositoryImpl
    ) : this(
        application = application,
        getSongsUseCase = GetSongsUseCase(musicRepository),
        addSongUseCase = AddSongUseCase(musicRepository),
        deleteSongUseCase = DeleteSongUseCase(musicRepository),
        searchSongsUseCase = SearchSongsUseCase(),
        getPlaylistsUseCase = GetPlaylistsUseCase(playlistRepository),
        createPlaylistUseCase = CreatePlaylistUseCase(playlistRepository),
        deletePlaylistUseCase = DeletePlaylistUseCase(playlistRepository),
        addSongToPlaylistUseCase = AddSongToPlaylistUseCase(playlistRepository),
        removeSongFromPlaylistUseCase = RemoveSongFromPlaylistUseCase(playlistRepository),
        playerController = MusicPlayerController(application)
    )

    val playerUiState: StateFlow<PlayerUiState> = playerController.uiState

    // User Profile / Friend Attribution State
    private val profilePrefs = application.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE)
    private val defaultInitialName = android.os.Build.MODEL.takeIf { !it.isNullOrBlank() } ?: "Friend"
    private val _userName = MutableStateFlow(
        profilePrefs.getString("key_user_name", null)?.takeIf { it.isNotBlank() } ?: defaultInitialName
    )
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _isEditProfileDialogOpen = MutableStateFlow(false)
    val isEditProfileDialogOpen: StateFlow<Boolean> = _isEditProfileDialogOpen.asStateFlow()

    fun openEditProfileDialog() {
        _isEditProfileDialogOpen.value = true
    }

    fun closeEditProfileDialog() {
        _isEditProfileDialogOpen.value = false
    }

    fun setUserName(name: String) {
        val cleanName = name.trim().ifEmpty { defaultInitialName }
        profilePrefs.edit().putString("key_user_name", cleanName).apply()
        _userName.value = cleanName
        _userMessage.value = "✓ Profile name updated to '$cleanName'"
        _isEditProfileDialogOpen.value = false
    }

    // Theme Mode Preference State (System, Light, Dark)
    private val themePrefs = application.getSharedPreferences("app_theme_prefs", Context.MODE_PRIVATE)
    private val _themeMode = MutableStateFlow(
        try {
            val saved = themePrefs.getString("key_theme_mode", ThemeMode.SYSTEM.name)
            ThemeMode.valueOf(saved ?: ThemeMode.SYSTEM.name)
        } catch (_: Exception) {
            ThemeMode.SYSTEM
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        themePrefs.edit().putString("key_theme_mode", mode.name).apply()
        _themeMode.value = mode
        val label = when (mode) {
            ThemeMode.SYSTEM -> "System Theme"
            ThemeMode.LIGHT -> "Light Mode"
            ThemeMode.DARK -> "Dark Mode"
        }
        _userMessage.value = "Theme set to $label"
    }

    fun toggleTheme() {
        val nextMode = when (_themeMode.value) {
            ThemeMode.SYSTEM -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
        }
        setThemeMode(nextMode)
    }

    // Tab Navigation State
    private val _selectedTab = MutableStateFlow(HomeTab.HOME)
    val selectedTab: StateFlow<HomeTab> = _selectedTab.asStateFlow()

    // Songs State
    val allSongs: StateFlow<List<Song>> = getSongsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchFilterTag = MutableStateFlow("All")
    val searchFilterTag: StateFlow<String> = _searchFilterTag.asStateFlow()

    val filteredSongs: StateFlow<List<Song>> = combine(allSongs, _searchQuery, _searchFilterTag) { songs, query, tag ->
        searchSongsUseCase(songs, query, tag)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Playlists State
    val playlists: StateFlow<List<Playlist>> = getPlaylistsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    // Dialog & UI Visibility States
    private val _isAddMusicDialogOpen = MutableStateFlow(false)
    val isAddMusicDialogOpen: StateFlow<Boolean> = _isAddMusicDialogOpen.asStateFlow()

    private val _isCreatePlaylistDialogOpen = MutableStateFlow(false)
    val isCreatePlaylistDialogOpen: StateFlow<Boolean> = _isCreatePlaylistDialogOpen.asStateFlow()

    private val _songForAddToPlaylist = MutableStateFlow<Song?>(null)
    val songForAddToPlaylist: StateFlow<Song?> = _songForAddToPlaylist.asStateFlow()

    private val _isFullPlayerVisible = MutableStateFlow(false)
    val isFullPlayerVisible: StateFlow<Boolean> = _isFullPlayerVisible.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        viewModelScope.launch {
            allSongs.collect { songs ->
                playerController.setPlaylist(songs)
            }
        }
    }

    fun selectTab(tab: HomeTab) {
        _selectedTab.value = tab
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onSearchFilterTagChanged(tag: String) {
        _searchFilterTag.value = tag
    }

    fun selectPlaylist(playlist: Playlist?) {
        _selectedPlaylist.value = playlist
    }

    fun openCreatePlaylistDialog() {
        _isCreatePlaylistDialogOpen.value = true
    }

    fun closeCreatePlaylistDialog() {
        _isCreatePlaylistDialogOpen.value = false
    }

    fun createPlaylist(name: String, description: String = "") {
        viewModelScope.launch {
            val result = createPlaylistUseCase(name, description)
            if (result.isSuccess) {
                _userMessage.value = "✓ Playlist '${result.getOrNull()?.name}' created!"
            } else {
                _userMessage.value = "⚠️ Failed to create playlist: ${result.exceptionOrNull()?.localizedMessage}"
            }
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            val result = deletePlaylistUseCase(playlist.id)
            if (result.isSuccess) {
                if (_selectedPlaylist.value?.id == playlist.id) {
                    _selectedPlaylist.value = null
                }
                _userMessage.value = "✓ Playlist '${playlist.name}' deleted"
            }
        }
    }

    fun openAddToPlaylistDialog(song: Song) {
        _songForAddToPlaylist.value = song
    }

    fun closeAddToPlaylistDialog() {
        _songForAddToPlaylist.value = null
    }

    fun addSongToPlaylist(playlist: Playlist, song: Song) {
        viewModelScope.launch {
            val result = addSongToPlaylistUseCase(playlist.id, song.id)
            if (result.isSuccess) {
                _userMessage.value = "✓ Added '${song.title}' to '${playlist.name}'"
                // Refresh selected playlist if currently open
                if (_selectedPlaylist.value?.id == playlist.id) {
                    _selectedPlaylist.value = playlist.copy(songIds = playlist.songIds + song.id)
                }
            }
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        viewModelScope.launch {
            val result = removeSongFromPlaylistUseCase(playlistId, songId)
            if (result.isSuccess) {
                _userMessage.value = "✓ Track removed from playlist"
                _selectedPlaylist.value?.let { current ->
                    if (current.id == playlistId) {
                        _selectedPlaylist.value = current.copy(songIds = current.songIds.filter { it != songId })
                    }
                }
            }
        }
    }

    fun playPlaylist(songs: List<Song>, startSong: Song? = null) {
        if (songs.isEmpty()) return
        val targetSong = startSong ?: songs.first()
        playerController.playSong(targetSong, songs)
        showFullPlayer()
    }

    fun openAddMusicDialog() {
        _isAddMusicDialogOpen.value = true
        _userMessage.value = null
    }

    fun closeAddMusicDialog() {
        _isAddMusicDialogOpen.value = false
    }

    fun showFullPlayer() {
        _isFullPlayerVisible.value = true
    }

    fun hideFullPlayer() {
        _isFullPlayerVisible.value = false
    }

    fun playSong(song: Song, customQueue: List<Song>? = null) {
        val queue = customQueue ?: filteredSongs.value
        playerController.playSong(song, queue)
        showFullPlayer()
    }

    fun togglePlayPause() {
        playerController.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playerController.seekTo(positionMs)
    }

    fun skipNext() {
        playerController.skipNext()
    }

    fun skipPrevious() {
        playerController.skipPrevious()
    }

    fun toggleShuffle() {
        playerController.toggleShuffle()
    }

    fun toggleRepeat() {
        playerController.toggleRepeat()
    }

    fun deleteSong(song: Song) {
        viewModelScope.launch {
            deleteSongUseCase(song.id)
        }
    }

    /**
     * Extracts duration, embedded cover art, and delegates to domain AddSongUseCase for upload / storage.
     */
    fun addTrack(
        title: String,
        artist: String,
        audioUri: Uri,
        coverBytes: ByteArray? = null,
        coverUri: Uri? = null,
        coverUrl: String? = null,
        addedBy: String? = null
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                // Extract metadata (duration and embedded picture) if not provided
                val extracted = AudioMetadataHelper.extractMetadata(getApplication(), audioUri)
                val durationMs = if (extracted.durationMs > 0L) extracted.durationMs else 0L
                val resolvedCoverBytes = coverBytes ?: extracted.embeddedPicture

                val finalTitle = title.trim().ifEmpty { extracted.title ?: "New Track" }
                val finalArtist = artist.trim().ifEmpty { extracted.artist ?: "My Music" }
                val finalAddedBy = (addedBy ?: _userName.value).trim().ifEmpty { _userName.value }

                // Save uploader name as profile name if changed
                if (finalAddedBy != _userName.value) {
                    profilePrefs.edit().putString("key_user_name", finalAddedBy).apply()
                    _userName.value = finalAddedBy
                }

                val result = addSongUseCase(
                    title = finalTitle,
                    artist = finalArtist,
                    audioUri = audioUri,
                    durationMs = durationMs,
                    coverBytes = resolvedCoverBytes,
                    coverUri = coverUri,
                    coverUrl = coverUrl,
                    addedBy = finalAddedBy
                )

                if (result.isSuccess) {
                    _userMessage.value = "✓ Song '$finalTitle' by $finalAddedBy synced with friends!"
                } else {
                    val error = result.exceptionOrNull()?.localizedMessage ?: "Unknown error"
                    _userMessage.value = "⚠️ Cloud sync error: $error"
                }
                _isAddMusicDialogOpen.value = false
            } catch (e: Exception) {
                _userMessage.value = "⚠️ Error adding song: ${e.localizedMessage}"
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }
}
