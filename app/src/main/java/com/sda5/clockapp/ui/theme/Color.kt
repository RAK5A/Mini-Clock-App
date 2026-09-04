package com.sda5.clockapp.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

/* Main Brand & Theme Colors */
val ClockPrimary = Color(0xFFFF5252)
val ClockPrimaryVariant = Color(0xFFFF7B00)
val ClockOnPrimary = Color(0xFFFFFFFF)
val ClockPrimaryContainer = Color(0xFF2E1517)
val ClockOnPrimaryContainer = Color(0xFFFF8A8A)

val ClockSecondary = Color(0xFF2C2C2E)
val ClockOnSecondary = Color(0xFFFFFFFF)

val ClockBackground = Color(0xFF090A0F)
val ClockOnBackground = Color(0xFFFFFFFF)
val ClockSurface = Color(0xFF13151D)
val ClockSurfaceElevated = Color(0xFF1C1F2B)
val ClockOnSurface = Color(0xFFFFFFFF)

val ClockMutedText = Color(0xFF9E9EB2)
val ClockSubtleBorder = Color(0xFF2C2F3E)
val ClockChipBg = Color(0xFF1E212D)
val ClockChipBorder = Color(0xFF32364A)
val ClockDisabledStartBg = Color(0xFF3D2323)
val ClockDisabledStartText = Color(0xFF8A5A5A)
val ClockPauseRed = Color(0xFFFF3B30)
val ClockLiveNotificationBg = Color(0xFF171925)
val ClockLiveIconBg = Color(0xFF635BFF)

/* Feature Accents */
val AlarmAccent = Color(0xFF8B5CF6)
val AlarmAccentGradientEnd = Color(0xFFD946EF)
val StopwatchAccent = Color(0xFF06B6D4)
val StopwatchAccentGradientEnd = Color(0xFF3B82F6)
val WorldClockAccent = Color(0xFFF59E0B)
val WorldClockAccentGradientEnd = Color(0xFFEF4444)

/* Glassmorphism Colors */
val GlassSurface = Color(0x1AFFFFFF)
val GlassBorder = Color(0x33FFFFFF)
val GlassHighlight = Color(0x1FFFFFFF)

/* Reusable Gradients */
val TimerGradientBrush = Brush.linearGradient(
    colors = listOf(Color(0xFFFF5252), Color(0xFFFF7B00))
)
val AlarmGradientBrush = Brush.linearGradient(
    colors = listOf(AlarmAccent, AlarmAccentGradientEnd)
)
val StopwatchGradientBrush = Brush.linearGradient(
    colors = listOf(StopwatchAccent, StopwatchAccentGradientEnd)
)
val WorldClockGradientBrush = Brush.linearGradient(
    colors = listOf(WorldClockAccent, WorldClockAccentGradientEnd)
)
