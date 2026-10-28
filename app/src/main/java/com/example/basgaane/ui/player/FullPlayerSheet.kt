package com.example.basgaane.ui.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.basgaane.domain.model.Song
import com.example.basgaane.ui.common.LiquidCrystalBackground
import com.example.basgaane.ui.theme.BackgroundDark
import java.util.Locale

/**
 * Full-screen modal player sheet displaying the rotating vinyl disc,
 * song information, slider progress, and playback controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerSheet(
    song: Song?,
    isPlaying: Boolean,
    progressMs: Long,
    durationMs: Long,
    isShuffle: Boolean,
    repeatMode: Int,
    isLiked: Boolean = false,
    onToggleLike: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onDismiss: () -> Unit
) {
    if (song == null) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }

    val currentPosition = if (isDraggingSlider) {
        sliderPosition.toLong()
    } else {
        progressMs
    }

    val infiniteTransition = rememberInfiniteTransition(label = "VinylRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotationAngle"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundDark,
        dragHandle = null
    ) {
        Scaffold(
            containerColor = BackgroundDark,
            contentWindowInsets = WindowInsets.safeDrawing
        ) { sheetPadding ->
            LiquidCrystalBackground(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(sheetPadding)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar
                    PlayerHeaderBar(
                        onDismiss = onDismiss,
                        onShare = onShare
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Vinyl / Album Art Disc
                    VinylDisc(
                        song = song,
                        isPlaying = isPlaying,
                        rotation = rotation
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Song Title, Artist & Friend Attribution
                    PlayerTrackInfo(
                        song = song,
                        isLiked = isLiked,
                        onToggleLike = onToggleLike,
                        onShare = onShare
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Progress Slider
                    PlayerProgressSlider(
                        currentPositionMs = currentPosition,
                        durationMs = durationMs,
                        onSeekStart = { isDraggingSlider = true },
                        onSeekChange = { sliderPosition = it },
                        onSeekEnd = {
                            isDraggingSlider = false
                            onSeek(it)
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Controls Row
                    PlayerPlaybackControlsRow(
                        isPlaying = isPlaying,
                        isShuffle = isShuffle,
                        repeatMode = repeatMode,
                        onPlayPause = onPlayPause,
                        onSkipPrevious = onSkipPrevious,
                        onSkipNext = onSkipNext,
                        onToggleShuffle = onToggleShuffle,
                        onToggleRepeat = onToggleRepeat
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

/**
 * Formats duration in milliseconds into mm:ss format.
 */
fun formatPlayerMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}
