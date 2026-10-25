package com.example.basgaane.ui.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.basgaane.ui.theme.BackgroundDark
import com.example.basgaane.ui.theme.CrystalBorderActive
import com.example.basgaane.ui.theme.CrystalBorderSubtle
import com.example.basgaane.ui.theme.PrismCyan
import com.example.basgaane.ui.theme.PrismMagenta
import com.example.basgaane.ui.theme.PrismViolet

/**
 * Renders an ambient iridescent liquid light mesh behind the content.
 * Glowing chromatic pools shimmer softly through translucent crystal cards.
 */
@Composable
fun LiquidCrystalBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidAuraPulse")
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Top-left glowing cyan/teal fluid pool
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrismCyan.copy(alpha = auraAlpha),
                            Color.Transparent
                        ),
                        radius = 900f
                    )
                )
        )

        // Mid-right glowing violet fluid pool
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrismViolet.copy(alpha = auraAlpha * 0.9f),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
        )

        // Bottom-center glowing magenta fluid pool
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            PrismMagenta.copy(alpha = auraAlpha * 0.6f),
                            Color.Transparent
                        ),
                        radius = 700f
                    )
                )
        )

        content()
    }
}

/**
 * Reusable modifier providing translucent obsidian glass fill with a 1dp
 * refractive iridescent liquid crystal border.
 */
fun Modifier.crystalGlassCard(
    shape: Shape = RoundedCornerShape(18.dp),
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
): Modifier {
    val borderBrush = if (isSelected) {
        Brush.linearGradient(CrystalBorderActive)
    } else {
        Brush.linearGradient(CrystalBorderSubtle)
    }

    val backgroundBrush = if (isSelected) {
        Brush.linearGradient(
            listOf(
                Color(0xF0182035),
                Color(0xE0121727)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0xEA121626),
                Color(0xD00C101C)
            )
        )
    }

    var mod = this
        .clip(shape)
        .background(backgroundBrush)
        .border(
            width = if (isSelected) 1.5.dp else 1.dp,
            brush = borderBrush,
            shape = shape
        )

    if (onClick != null) {
        mod = mod.clickable { onClick() }
    }

    return mod
}

/**
 * Reusable modifier for liquid crystal pill buttons, chips, and capsules.
 */
fun Modifier.crystalPill(
    shape: Shape = RoundedCornerShape(24.dp),
    isActive: Boolean = false,
    onClick: (() -> Unit)? = null
): Modifier {
    val borderBrush = if (isActive) {
        Brush.linearGradient(CrystalBorderActive)
    } else {
        Brush.linearGradient(CrystalBorderSubtle)
    }

    val bgBrush = if (isActive) {
        Brush.linearGradient(
            listOf(
                PrismViolet.copy(alpha = 0.35f),
                PrismCyan.copy(alpha = 0.25f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color(0x99171D2F),
                Color(0x660E121E)
            )
        )
    }

    var mod = this
        .clip(shape)
        .background(bgBrush)
        .border(width = 1.dp, brush = borderBrush, shape = shape)

    if (onClick != null) {
        mod = mod.clickable { onClick() }
    }

    return mod
}
