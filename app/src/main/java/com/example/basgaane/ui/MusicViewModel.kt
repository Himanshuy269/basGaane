package com.example.basgaane.ui

import android.app.Application
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.basgaane.data.DefaultMusicRepository
import com.example.basgaane.data.MusicRepository
import com.example.basgaane.model.Song
import com.example.basgaane.player.MusicPlayerController
import com.example.basgaane.player.PlayerUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MusicRepository = DefaultMusicRepository(application)
    private val playerController: MusicPlayerController = MusicPlayerController(application)

    val playerUiState: StateFlow<PlayerUiState> = playerController.uiState

    val allSongs: StateFlow<List<Song>> = repository.getSongs().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredSongs: StateFlow<List<Song>> = combine(allSongs, _searchQuery) { songs, query ->
        if (query.isBlank()) {
            songs
        } else {
            songs.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.artist.contains(query, ignoreCase = true)
            }
        }
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
            repository.removeSong(song.id)
        }
    }

    /**
     * Extracts duration and metadata from picked audio URI, then saves to repository.
     */
    fun addLocalTrack(
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
                    // Fallback to 0 if retriever fails
                }

                val finalTitle = title.trim().ifEmpty { "New Track" }
                val finalArtist = artist.trim().ifEmpty { "My Music" }

                val newSong = Song(
                    title = finalTitle,
                    artist = finalArtist,
                    audioUrl = audioUri.toString(),
                    coverUrl = coverUrl,
                    durationMs = durationMs,
                    isLocal = true,
                    addedAt = System.currentTimeMillis()
                )

                repository.addSong(newSong)
                _uploadStatusMessage.value = "Song '$finalTitle' added successfully!"
                _isAddMusicDialogOpen.value = false
            } catch (e: Exception) {
                _uploadStatusMessage.value = "Failed to add song: ${e.localizedMessage}"
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
