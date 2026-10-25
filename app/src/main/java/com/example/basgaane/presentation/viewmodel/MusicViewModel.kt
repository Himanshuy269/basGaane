package com.example.basgaane.presentation.viewmodel

import android.app.Application
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.basgaane.core.player.MusicPlayerController
import com.example.basgaane.core.player.PlayerUiState
import com.example.basgaane.data.repository.MusicRepositoryImpl
import com.example.basgaane.domain.model.Song
import com.example.basgaane.domain.usecase.AddSongUseCase
import com.example.basgaane.domain.usecase.DeleteSongUseCase
import com.example.basgaane.domain.usecase.GetSongsUseCase
import com.example.basgaane.domain.usecase.SearchSongsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Presentation ViewModel coordinating domain Use Cases and exposing UI state.
 */
class MusicViewModel(
    application: Application,
    private val getSongsUseCase: GetSongsUseCase,
    private val addSongUseCase: AddSongUseCase,
    private val deleteSongUseCase: DeleteSongUseCase,
    private val searchSongsUseCase: SearchSongsUseCase,
    private val playerController: MusicPlayerController
) : AndroidViewModel(application) {

    // Default factory constructor for ViewModelProvider
    constructor(application: Application) : this(
        application = application,
        repository = MusicRepositoryImpl(application)
    )

    private constructor(
        application: Application,
        repository: MusicRepositoryImpl
    ) : this(
        application = application,
        getSongsUseCase = GetSongsUseCase(repository),
        addSongUseCase = AddSongUseCase(repository),
        deleteSongUseCase = DeleteSongUseCase(repository),
        searchSongsUseCase = SearchSongsUseCase(),
        playerController = MusicPlayerController(application)
    )

    val playerUiState: StateFlow<PlayerUiState> = playerController.uiState

    val allSongs: StateFlow<List<Song>> = getSongsUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredSongs: StateFlow<List<Song>> = combine(allSongs, _searchQuery) { songs, query ->
        searchSongsUseCase(songs, query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isAddMusicDialogOpen = MutableStateFlow(false)
    val isAddMusicDialogOpen: StateFlow<Boolean> = _isAddMusicDialogOpen.asStateFlow()

    private val _isFullPlayerVisible = MutableStateFlow(false)
    val isFullPlayerVisible: StateFlow<Boolean> = _isFullPlayerVisible.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _uploadStatusMessage = MutableStateFlow<String?>(null)
    val uploadStatusMessage: StateFlow<String?> = _uploadStatusMessage.asStateFlow()

    init {
        viewModelScope.launch {
            allSongs.collect { songs ->
                playerController.setPlaylist(songs)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun openAddMusicDialog() {
        _isAddMusicDialogOpen.value = true
        _uploadStatusMessage.value = null
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

    fun playSong(song: Song) {
        playerController.playSong(song, filteredSongs.value)
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
     * Extracts duration and delegates to domain AddSongUseCase for upload / storage.
     */
    fun addTrack(
        title: String,
        artist: String,
        audioUri: Uri,
        coverUrl: String? = null
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                var durationMs = 0L
                try {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(getApplication(), audioUri)
                    val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    durationMs = time?.toLongOrNull() ?: 0L
                    retriever.release()
                } catch (e: Exception) {
                    // Fallback if retriever cannot parse
                }

                val finalTitle = title.trim().ifEmpty { "New Track" }
                val finalArtist = artist.trim().ifEmpty { "My Music" }

                val result = addSongUseCase(
                    title = finalTitle,
                    artist = finalArtist,
                    audioUri = audioUri,
                    durationMs = durationMs,
                    coverUrl = coverUrl
                )

                if (result.isSuccess) {
                    _uploadStatusMessage.value = "Song '$finalTitle' added and synced!"
                } else {
                    _uploadStatusMessage.value = "Song saved locally."
                }
                _isAddMusicDialogOpen.value = false
            } catch (e: Exception) {
                _uploadStatusMessage.value = "Error adding song: ${e.localizedMessage}"
            } finally {
                _isUploading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }
}
