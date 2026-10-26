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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.basgaane.ui.theme.LocalBasGaaneColors
import com.example.basgaane.ui.theme.PrismCyan
import com.example.basgaane.ui.theme.PrismMagenta
import com.example.basgaane.ui.theme.PrismViolet

// Static shared shapes to prevent allocation thrashing during list scrolling
val CrystalGlassCardShape = RoundedCornerShape(18.dp)
val CrystalPillShape = RoundedCornerShape(24.dp)
val CrystalBadgeShape = RoundedCornerShape(6.dp)
val CrystalCoverShape = RoundedCornerShape(14.dp)

/**
 * High-performance ambient iridescent liquid light mesh.
 * Renders entirely in the Draw phase via [Modifier.drawBehind] so animation
 * pulses do NOT cause layout or composition passes, ensuring 120 FPS buttery smooth scrolling.
 */
@Composable
fun LiquidCrystalBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = LocalBasGaaneColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidAuraPulse")
    val auraAlpha = infiniteTransition.animateFloat(
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
    val bgColor = colors.background

    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind {
                // 1. Solid canvas background
                drawRect(color = bgColor)

                val alpha = auraAlpha.value
                val w = size.width
                val h = size.height

                // 2. Top-left fluid pool
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(cyanAura.copy(alpha = alpha), Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = 900f
                    ),
                    radius = 900f,
                    center = Offset(0f, 0f)
                )

                // 3. Mid-right fluid pool
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(violetAura.copy(alpha = alpha * 0.9f), Color.Transparent),
                        center = Offset(w, h * 0.38f),
                        radius = 800f
                    ),
                    radius = 800f,
                    center = Offset(w, h * 0.38f)
                )

                // 4. Bottom-center fluid pool
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(magentaAura.copy(alpha = alpha * 0.6f), Color.Transparent),
                        center = Offset(w * 0.5f, h),
                        radius = 700f
                    ),
                    radius = 700f,
                    center = Offset(w * 0.5f, h)
                )
            }
    ) {
        content()
    }
}

/**
 * Reusable modifier providing translucent crystal glass fill with a refractive
 * iridescent border. Uses pre-allocated Brushes from [BasGaaneColors] to eliminate
 * GC overhead while flinging or scrolling.
 */
@Composable
fun Modifier.crystalGlassCard(
    shape: Shape = CrystalGlassCardShape,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
): Modifier {
    val colors = LocalBasGaaneColors.current
    val borderBrush = if (isSelected) colors.borderActiveBrush else colors.borderSubtleBrush
    val bgBrush = if (isSelected) colors.cardBackgroundActiveBrush else colors.cardBackgroundBrush

    val base = this
        .clip(shape)
        .background(bgBrush)
        .border(
            width = if (isSelected) 1.5.dp else 1.dp,
            brush = borderBrush,
            shape = shape
        )

    return if (onClick != null) base.clickable(onClick = onClick) else base
}

/**
 * Reusable modifier for liquid crystal pill buttons, chips, and capsules.
 * Zero object allocations on scroll.
 */
@Composable
fun Modifier.crystalPill(
    shape: Shape = CrystalPillShape,
    isActive: Boolean = false,
    onClick: (() -> Unit)? = null
): Modifier {
    val colors = LocalBasGaaneColors.current
    val borderBrush = if (isActive) colors.borderActiveBrush else colors.borderSubtleBrush
    val bgBrush = if (isActive) colors.pillActiveBgBrush else colors.pillInactiveBgBrush

    val base = this
        .clip(shape)
        .background(bgBrush)
        .border(width = 1.dp, brush = borderBrush, shape = shape)

    return if (onClick != null) base.clickable(onClick = onClick) else base
}
