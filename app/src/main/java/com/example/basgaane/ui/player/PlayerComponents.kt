package com.example.basgaane.ui.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.basgaane.R
import com.example.basgaane.domain.model.Song
import com.example.basgaane.ui.common.SongCoverImage
import com.example.basgaane.ui.common.crystalPill
import com.example.basgaane.ui.theme.PlayerTrack
import com.example.basgaane.ui.theme.PrismBlue
import com.example.basgaane.ui.theme.PrismCyan
import com.example.basgaane.ui.theme.PrismMagenta
import com.example.basgaane.ui.theme.PrismViolet
import com.example.basgaane.ui.theme.TextPrimary
import com.example.basgaane.ui.theme.TextSecondary
import com.example.basgaane.ui.theme.TextTertiary

/**
 * Header top bar of the full-screen player sheet.
 */
@Composable
fun PlayerHeaderBar(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Collapse",
                tint = TextPrimary,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = "NOW PLAYING",
            style = MaterialTheme.typography.labelLarge.copy(
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            ),
            color = PrismCyan
        )

        Spacer(modifier = Modifier.size(32.dp))
    }
}

/**
 * Animated liquid-crystal rotating vinyl disc.
 */
@Composable
fun VinylDisc(
    song: Song,
    isPlaying: Boolean,
    rotation: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        // Ambient chromatic fluid glow
        Box(
            modifier = Modifier
                .size(270.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrismCyan.copy(alpha = 0.35f),
                            PrismViolet.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Prismatic Vinyl Disc
        Box(
            modifier = Modifier
                .size(250.dp)
                .shadow(20.dp, CircleShape)
                .clip(CircleShape)
                .background(Color(0xFF0F121C))
                .border(
                    width = 3.dp,
                    brush = Brush.sweepGradient(
                        listOf(PrismCyan, PrismBlue, PrismViolet, PrismMagenta, PrismCyan)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF131724))
                    .rotate(if (isPlaying) rotation else 0f),
                contentAlignment = Alignment.Center
            ) {
                SongCoverImage(
                    song = song,
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape),
                    shape = CircleShape,
                    contentScale = ContentScale.Crop
                )

                // Center spindle
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0B0E17))
                        .border(2.dp, PrismCyan.copy(alpha = 0.6f), CircleShape)
                )
            }
        }
    }
}

/**
 * Track title, artist name, like button, and contributor attribution pill.
 */
@Composable
fun PlayerTrackInfo(
    song: Song,
    isLiked: Boolean = false,
    onToggleLike: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onToggleLike != null) {
                Spacer(modifier = Modifier.size(40.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }

            if (onToggleLike != null) {
                IconButton(
                    onClick = onToggleLike,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isLiked) "Unlike" else "Like",
                        tint = if (isLiked) PrismMagenta else TextTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .crystalPill(
                        shape = RoundedCornerShape(8.dp),
                        isActive = !song.isLocal
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (song.isLocal) Icons.Default.PhoneAndroid else Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = if (song.isLocal) TextSecondary else PrismCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (song.isLocal) "Local" else "Shared",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (song.isLocal) TextSecondary else PrismCyan
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Friend attribution pill
        Box(
            modifier = Modifier
                .crystalPill(
                    shape = RoundedCornerShape(12.dp),
                    isActive = true
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = PrismCyan,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Added by ${song.addedBy}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Playback slider control with elapsed and total duration timestamps.
 */
@Composable
fun PlayerProgressSlider(
    currentPositionMs: Long,
    durationMs: Long,
    onSeekStart: () -> Unit,
    onSeekChange: (Float) -> Unit,
    onSeekEnd: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxDuration = durationMs.coerceAtLeast(1L).toFloat()

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = currentPositionMs.toFloat().coerceIn(0f, maxDuration),
            onValueChange = {
                onSeekStart()
                onSeekChange(it)
            },
            onValueChangeFinished = {
                onSeekEnd(currentPositionMs)
            },
            valueRange = 0f..maxDuration,
            colors = SliderDefaults.colors(
                thumbColor = PrismCyan,
                activeTrackColor = PrismCyan,
                inactiveTrackColor = PlayerTrack
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatPlayerMs(currentPositionMs),
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary
            )
            Text(
                text = formatPlayerMs(durationMs),
                style = MaterialTheme.typography.labelMedium,
                color = TextTertiary
            )
        }
    }
}

/**
 * Media playback control buttons (Shuffle, Previous, Play/Pause, Next, Repeat).
 */
@Composable
fun PlayerPlaybackControlsRow(
    isPlaying: Boolean,
    isShuffle: Boolean,
    repeatMode: Int,
    onPlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .crystalPill(shape = CircleShape, isActive = isShuffle, onClick = onToggleShuffle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                tint = if (isShuffle) Color.White else TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .crystalPill(shape = CircleShape, isActive = false, onClick = onSkipPrevious),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                tint = TextPrimary,
                modifier = Modifier.size(28.dp)
            )
        }

        // Play / Pause glowing center button
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(PrismViolet, PrismCyan)
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        listOf(PrismCyan, PrismMagenta)
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(70.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .crystalPill(shape = CircleShape, isActive = false, onClick = onSkipNext),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next",
                tint = TextPrimary,
                modifier = Modifier.size(28.dp)
            )
        }

        val repeatIcon = if (repeatMode == Player.REPEAT_MODE_ONE) {
            Icons.Default.RepeatOne
        } else {
            Icons.Default.Repeat
        }
        val isRepeatActive = repeatMode != Player.REPEAT_MODE_OFF

        Box(
            modifier = Modifier
                .size(42.dp)
                .crystalPill(shape = CircleShape, isActive = isRepeatActive, onClick = onToggleRepeat),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = repeatIcon,
                contentDescription = "Repeat",
                tint = if (isRepeatActive) Color.White else TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
