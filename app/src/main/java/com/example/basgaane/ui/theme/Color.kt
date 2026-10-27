package com.example.basgaane.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// Noctra Minimal Dark & Lime Design System Tokens
val NoctraLime = Color(0xFFD2F844)
val NoctraLimeDark = Color(0xFF1E2812)
val NoctraDark = Color(0xFF0C0E12)
val NoctraSurface = Color(0xFF141720)
val NoctraSurfaceElevated = Color(0xFF1C202C)
val NoctraBorder = Color(0xFF222735)
val NoctraChipInactive = Color(0xFF191D28)
val NoctraTextPrimary = Color(0xFFFFFFFF)
val NoctraTextSecondary = Color(0xFF8B92A2)
val NoctraTextMuted = Color(0xFF5A6273)
val NoctraTrackBar = Color(0xFF232838)

// Prismatic / Iridescent Spectrum Colors
val PrismCyan = Color(0xFF00F2FE)
val PrismBlue = Color(0xFF4FACFE)
val PrismViolet = Color(0xFF8B5CF6)
val PrismMagenta = Color(0xFFFF2A85)
val PrismEmerald = Color(0xFF10B981)

// Neon & Accent Colors
val NeonPurple = Color(0xFF8B5CF6)
val NeonPurpleLight = Color(0xFFA78BFA)
val NeonCyan = Color(0xFF00F2FE)
val NeonPink = Color(0xFFFF2A85)
val ElectricBlue = Color(0xFF3B82F6)

// Functional Accent Gradients
val AccentGradientStart = Color(0xFF8B5CF6)
val AccentGradientEnd = Color(0xFF00F2FE)

val LiquidCrystalGradient = listOf(
    PrismCyan,
    PrismBlue,
    PrismViolet,
    PrismMagenta
)

val LiquidProgressGradient = listOf(
    PrismCyan,
    PrismViolet,
    PrismMagenta
)

// Dynamic Theme-backed Liquid Crystal Colors
val BackgroundDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBasGaaneColors.current.background

val SurfaceDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBasGaaneColors.current.surface

val SurfaceDarkVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBasGaaneColors.current.surfaceVariant

val SurfaceLightDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBasGaaneColors.current.surfaceLightDark

val CrystalGlassFill: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBasGaaneColors.current.glassFill

val CrystalGlassFillLight: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBasGaaneColors.current.glassFillLight

val CrystalGlassCard: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBasGaaneColors.current.glassCard

val CrystalGlassCardActive: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBasGaaneColors.current.glassCardActive

val TextPrimary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBasGaaneColors.current.textPrimary

val TextSecondary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBasGaaneColors.current.textSecondary

val TextTertiary: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBasGaaneColors.current.textTertiary

val PlayerTrack: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBasGaaneColors.current.playerTrack

val CrystalBorderSubtle: List<Color>
    @Composable
    @ReadOnlyComposable
    get() = LocalBasGaaneColors.current.borderSubtle

val CrystalBorderActive: List<Color>
    @Composable
    @ReadOnlyComposable
    get() = LocalBasGaaneColors.current.borderActive

val InputFieldContainerFocused: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBasGaaneColors.current.inputContainerFocused

val InputFieldContainerUnfocused: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBasGaaneColors.current.inputContainerUnfocused

val InputBorderUnfocused: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBasGaaneColors.current.inputBorderUnfocused
