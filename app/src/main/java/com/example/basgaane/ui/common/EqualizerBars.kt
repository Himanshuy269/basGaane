package com.example.basgaane.ui.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.basgaane.ui.theme.NeonCyan

/**
 * High-performance animated equalizer visualizer bars for currently playing songs.
 * Draws directly onto Canvas to bypass layout measurement passes.
 */
@Composable
fun EqualizerBars(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = NeonCyan,
    maxHeight: Dp = 18.dp,
    barWidth: Dp = 3.dp,
    spacing: Dp = 2.dp
) {
    val transition = rememberInfiniteTransition(label = "EqualizerTransition")

    val h1 by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = if (isPlaying) 1f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar1"
    )

    val h2 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = if (isPlaying) 0.3f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar2"
    )

    val h3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (isPlaying) 0.95f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 480, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar3"
    )

    val totalWidth = (barWidth * 3) + (spacing * 2)

    Canvas(modifier = modifier.size(width = totalWidth, height = maxHeight)) {
        val barW = barWidth.toPx()
        val spaceW = spacing.toPx()
        val totalH = size.height
        val corner = CornerRadius(barW / 2f, barW / 2f)

        val heights = floatArrayOf(h1, h2, h3)
        for (i in 0..2) {
            val h = totalH * heights[i]
            val x = i * (barW + spaceW)
            val y = totalH - h
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barW, h),
                cornerRadius = corner
            )
        }
    }
}
