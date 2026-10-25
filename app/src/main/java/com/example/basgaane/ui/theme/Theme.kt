package com.example.basgaane.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val BasGaaneColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = TextPrimary,
    primaryContainer = SurfaceDarkVariant,
    onPrimaryContainer = NeonPurpleLight,
    secondary = NeonCyan,
    onSecondary = BackgroundDark,
    secondaryContainer = SurfaceLightDark,
    onSecondaryContainer = NeonCyan,
    tertiary = NeonPink,
    background = BackgroundDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceDarkVariant,
    onSurfaceVariant = TextSecondary,
    outline = PlayerTrack
)

@Composable
fun BasGaaneTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BasGaaneColorScheme,
        typography = Typography,
        content = content
    )
}
