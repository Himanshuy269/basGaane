package com.example.basgaane.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Liquid Crystal design system color tokens supporting both
 * Obsidian Dark Mode and Luminous Frosted Opal Light Mode.
 */
data class BasGaaneColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceLightDark: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val glassFill: Color,
    val glassFillLight: Color,
    val glassCard: Color,
    val glassCardActive: Color,
    val playerTrack: Color,
    val cardBackgroundStart: Color,
    val cardBackgroundEnd: Color,
    val cardBackgroundActiveStart: Color,
    val cardBackgroundActiveEnd: Color,
    val pillInactiveBgStart: Color,
    val pillInactiveBgEnd: Color,
    val borderSubtle: List<Color>,
    val borderActive: List<Color>,
    val inputContainerFocused: Color,
    val inputContainerUnfocused: Color,
    val inputBorderUnfocused: Color
)

val DarkBasGaaneColors = BasGaaneColors(
    isDark = true,
    background = Color(0xFF07090E),
    surface = Color(0xFF111522),
    surfaceVariant = Color(0xFF192033),
    surfaceLightDark = Color(0xFF222B42),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    textTertiary = Color(0xFF64748B),
    glassFill = Color(0xE6121727),
    glassFillLight = Color(0xB81B2236),
    glassCard = Color(0xDB131828),
    glassCardActive = Color(0xF0182035),
    playerTrack = Color(0xFF232D42),
    cardBackgroundStart = Color(0xEA121626),
    cardBackgroundEnd = Color(0xD00C101C),
    cardBackgroundActiveStart = Color(0xF0182035),
    cardBackgroundActiveEnd = Color(0xE0121727),
    pillInactiveBgStart = Color(0x99171D2F),
    pillInactiveBgEnd = Color(0x660E121E),
    borderSubtle = listOf(
        Color(0x4500F2FE),
        Color(0x258B5CF6),
        Color(0x35FF2A85),
        Color(0x1EFFFFFF)
    ),
    borderActive = listOf(
        Color(0xFF00F2FE), // PrismCyan
        Color(0xFF8B5CF6), // PrismViolet
        Color(0xFFFF2A85), // PrismMagenta
        Color(0xFF00F2FE)  // PrismCyan
    ),
    inputContainerFocused = Color(0x600C101C),
    inputContainerUnfocused = Color(0x400C101C),
    inputBorderUnfocused = Color(0x33FFFFFF)
)

val LightBasGaaneColors = BasGaaneColors(
    isDark = false,
    background = Color(0xFFF4F6FB),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE8EEF8),
    surfaceLightDark = Color(0xFFDCE4F0),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textTertiary = Color(0xFF94A3B8),
    glassFill = Color(0xF0FFFFFF),
    glassFillLight = Color(0xD9FFFFFF),
    glassCard = Color(0xF5FFFFFF),
    glassCardActive = Color(0xFFEEF2FF),
    playerTrack = Color(0xFFE2E8F0),
    cardBackgroundStart = Color(0xFAFFFFFF),
    cardBackgroundEnd = Color(0xEDF1F5F9),
    cardBackgroundActiveStart = Color(0xFFF3F0FF),
    cardBackgroundActiveEnd = Color(0xFFEFF6FF),
    pillInactiveBgStart = Color(0xF5FFFFFF),
    pillInactiveBgEnd = Color(0xE5E2E8F0),
    borderSubtle = listOf(
        Color(0x5500B4D8),
        Color(0x408B5CF6),
        Color(0x40FF2A85),
        Color(0x3064748B)
    ),
    borderActive = listOf(
        Color(0xFF0284C7),
        Color(0xFF7C3AED),
        Color(0xFFDB2777),
        Color(0xFF0284C7)
    ),
    inputContainerFocused = Color(0x28CBD5E1),
    inputContainerUnfocused = Color(0x18CBD5E1),
    inputBorderUnfocused = Color(0x3364748B)
)

val LocalBasGaaneColors = staticCompositionLocalOf { DarkBasGaaneColors }

private val DarkBasGaaneColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = Color(0xFFF8FAFC),
    primaryContainer = Color(0xFF192033),
    onPrimaryContainer = NeonPurpleLight,
    secondary = NeonCyan,
    onSecondary = Color(0xFF07090E),
    secondaryContainer = Color(0xFF222B42),
    onSecondaryContainer = NeonCyan,
    tertiary = NeonPink,
    background = Color(0xFF07090E),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF111522),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF192033),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF232D42)
)

private val LightBasGaaneColorScheme = lightColorScheme(
    primary = Color(0xFF7C3AED),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FE),
    onPrimaryContainer = Color(0xFF5B21B6),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = Color(0xFFDB2777),
    background = Color(0xFFF4F6FB),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFE8EEF8),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1)
)

@Composable
fun BasGaaneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkBasGaaneColors else LightBasGaaneColors
    val colorScheme = if (darkTheme) DarkBasGaaneColorScheme else LightBasGaaneColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalBasGaaneColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BasGaaneTypography,
            content = content
        )
    }
}
