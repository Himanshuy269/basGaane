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
import com.example.basgaane.ui.theme.LocalBasGaaneColors
import com.example.basgaane.ui.theme.PrismCyan
import com.example.basgaane.ui.theme.PrismMagenta
import com.example.basgaane.ui.theme.PrismViolet

/**
 * Renders an ambient iridescent liquid light mesh behind the content.
 * Shimmers between Obsidian Nebula (Dark Mode) and Frosted Opal Prism (Light Mode).
 */
@Composable
fun LiquidCrystalBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = LocalBasGaaneColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidAuraPulse")
    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = if (colors.isDark) 0.18f else 0.10f,
        targetValue = if (colors.isDark) 0.28f else 0.20f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "AuraAlpha"
    )

    val cyanAura = if (colors.isDark) PrismCyan else Color(0xFF38BDF8)
    val violetAura = if (colors.isDark) PrismViolet else Color(0xFFA78BFA)
    val magentaAura = if (colors.isDark) PrismMagenta else Color(0xFFFB7185)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Top-left glowing fluid pool
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            cyanAura.copy(alpha = auraAlpha),
                            Color.Transparent
                        ),
                        radius = 900f
                    )
                )
        )

        // Mid-right glowing fluid pool
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            violetAura.copy(alpha = auraAlpha * 0.9f),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
        )

        // Bottom-center glowing fluid pool
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            magentaAura.copy(alpha = auraAlpha * 0.6f),
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
 * Reusable modifier providing translucent crystal glass fill with a refractive
 * iridescent border that adapts smoothly between Light and Dark modes.
 */
@Composable
fun Modifier.crystalGlassCard(
    shape: Shape = RoundedCornerShape(18.dp),
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
): Modifier {
    val colors = LocalBasGaaneColors.current
    val borderBrush = if (isSelected) {
        Brush.linearGradient(colors.borderActive)
    } else {
        Brush.linearGradient(colors.borderSubtle)
    }

    val backgroundBrush = if (isSelected) {
        Brush.linearGradient(
            listOf(
                colors.cardBackgroundActiveStart,
                colors.cardBackgroundActiveEnd
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                colors.cardBackgroundStart,
                colors.cardBackgroundEnd
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
@Composable
fun Modifier.crystalPill(
    shape: Shape = RoundedCornerShape(24.dp),
    isActive: Boolean = false,
    onClick: (() -> Unit)? = null
): Modifier {
    val colors = LocalBasGaaneColors.current
    val borderBrush = if (isActive) {
        Brush.linearGradient(colors.borderActive)
    } else {
        Brush.linearGradient(colors.borderSubtle)
    }

    val bgBrush = if (isActive) {
        Brush.linearGradient(
            if (colors.isDark) {
                listOf(
                    PrismViolet.copy(alpha = 0.35f),
                    PrismCyan.copy(alpha = 0.25f)
                )
            } else {
                listOf(
                    Color(0xFF7C3AED).copy(alpha = 0.20f),
                    Color(0xFF0284C7).copy(alpha = 0.15f)
                )
            }
        )
    } else {
        Brush.linearGradient(
            listOf(
                colors.pillInactiveBgStart,
                colors.pillInactiveBgEnd
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
