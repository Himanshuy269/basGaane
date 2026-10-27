package com.example.basgaane.core.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.basgaane.core.service.MusicPlaybackService
import com.example.basgaane.domain.model.Song
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val playlist: List<Song> = emptyList()
)

class MusicPlayerController(
    private val context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + Job())
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var progressJob: Job? = null

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    var onPlaybackEnded: (() -> Unit)? = null
    var onPlaybackError: ((Exception) -> Unit)? = null

    private val playerListener = object : Player.Listener {
        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            android.util.Log.e("MusicPlayerController", "Playback exception occurred: ${error.errorCodeName} - ${error.message}", error)
            _uiState.update { it.copy(isPlaying = false, isBuffering = false) }
            onPlaybackError?.invoke(error)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _uiState.update { it.copy(isPlaying = isPlaying) }
            if (isPlaying) {
                startProgressTracker()
            } else {
                stopProgressTracker()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val isBuffering = playbackState == Player.STATE_BUFFERING
            val duration = mediaController?.duration?.takeIf { it > 0 } ?: _uiState.value.durationMs
            _uiState.update {
                it.copy(
                    isBuffering = isBuffering,
                    durationMs = duration
                )
            }

            if (playbackState == Player.STATE_ENDED) {
                if (_uiState.value.repeatMode == Player.REPEAT_MODE_ONE) {
                    mediaController?.seekTo(0L)
                    mediaController?.play()
                } else {
                    onPlaybackEnded?.invoke()
                }
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val mediaId = mediaItem?.mediaId
            val matchedSong = _uiState.value.playlist.find { it.id == mediaId }
            val duration = mediaController?.duration?.takeIf { it > 0 }
                ?: matchedSong?.durationMs
                ?: 0L

            _uiState.update {
                it.copy(
                    currentSong = matchedSong ?: it.currentSong,
                    durationMs = duration,
                    currentPositionMs = 0L
                )
            }
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _uiState.update { it.copy(isShuffleEnabled = shuffleModeEnabled) }
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _uiState.update { it.copy(repeatMode = repeatMode) }
        }
    }

    init {
        initController()
    }

    private fun initController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicPlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()?.apply {
                    addListener(playerListener)
                    _uiState.update {
                        it.copy(
                            isPlaying = isPlaying,
                            isShuffleEnabled = shuffleModeEnabled,
                            repeatMode = repeatMode
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun setPlaylist(songs: List<Song>) {
        _uiState.update { it.copy(playlist = songs) }
    }

    fun playSong(song: Song, playlist: List<Song> = _uiState.value.playlist) {
        val controller = mediaController ?: return

        val songsList = if (playlist.contains(song)) playlist else listOf(song) + playlist
        _uiState.update { it.copy(playlist = songsList, currentSong = song) }

        val mediaItems = songsList.map { it.toMediaItem() }
        val startIndex = songsList.indexOfFirst { it.id == song.id }.coerceAtLeast(0)

        controller.setMediaItems(mediaItems, startIndex, 0L)
        controller.prepare()
        controller.play()
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            if (controller.playbackState == Player.STATE_IDLE && _uiState.value.currentSong != null) {
                _uiState.value.currentSong?.let { playSong(it) }
            } else {
                controller.play()
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _uiState.update { it.copy(currentPositionMs = positionMs) }
    }

    fun skipNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        } else if (_uiState.value.playlist.isNotEmpty()) {
            val currentIndex = _uiState.value.playlist.indexOfFirst { it.id == _uiState.value.currentSong?.id }
            val nextIndex = (currentIndex + 1) % _uiState.value.playlist.size
            playSong(_uiState.value.playlist[nextIndex])
        }
    }

    fun skipPrevious() {
        val controller = mediaController ?: return
        if (controller.currentPosition > 3000L) {
            controller.seekTo(0L)
        } else if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
        } else if (_uiState.value.playlist.isNotEmpty()) {
            val currentIndex = _uiState.value.playlist.indexOfFirst { it.id == _uiState.value.currentSong?.id }
            val prevIndex = if (currentIndex <= 0) _uiState.value.playlist.size - 1 else currentIndex - 1
            playSong(_uiState.value.playlist[prevIndex])
        }
    }

    fun toggleShuffle() {
        val controller = mediaController ?: return
        val newShuffle = !controller.shuffleModeEnabled
        controller.shuffleModeEnabled = newShuffle
        _uiState.update { it.copy(isShuffleEnabled = newShuffle) }
    }

    fun toggleRepeat() {
        val controller = mediaController ?: return
        val newRepeat = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        controller.repeatMode = newRepeat
        _uiState.update { it.copy(repeatMode = newRepeat) }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        progressJob = coroutineScope.launch {
            while (isActive) {
                mediaController?.let { controller ->
                    val pos = controller.currentPosition
                    val duration = controller.duration.takeIf { it > 0 } ?: _uiState.value.durationMs
                    _uiState.update {
                        it.copy(
                            currentPositionMs = pos,
                            durationMs = duration
                        )
                    }
                }
                delay(300)
            }
        }
    }

    private fun stopProgressTracker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressTracker()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
    }

    private fun Song.toMediaItem(): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setArtworkUri(coverUrl?.let { Uri.parse(it) })
            .build()

        return MediaItem.Builder()
            .setMediaId(id)
            .setUri(audioUrl)
            .setMediaMetadata(metadata)
            .build()
    }
}
