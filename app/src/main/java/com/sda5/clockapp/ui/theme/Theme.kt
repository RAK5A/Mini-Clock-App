package com.sda5.clockapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


/* Light theme for the project */
private val LightColors = lightColorScheme(
    primary = ClockPrimary,
    onPrimary = ClockOnPrimary,
    primaryContainer = ClockPrimaryContainer,
    background = ClockBackground,
    onBackground = ClockOnBackground,
    surface = ClockSurface,
    onSurface = ClockOnSurface
)

private val DarkColors = darkColorScheme(
    primary = ClockPrimary,
    onPrimary = ClockOnPrimary,
    primaryContainer = ClockPrimaryContainer,
    onPrimaryContainer = ClockOnPrimaryContainer,
    secondary = ClockSecondary,
    onSecondary = ClockOnSecondary,
    background = ClockBackground,
    onBackground = ClockOnBackground,
    surface = ClockSurface,
    onSurface = ClockOnSurface,
    surfaceVariant = ClockChipBg,
    onSurfaceVariant = ClockMutedText
)

// TODO(dark theme): add a DarkColors scheme + isSystemInDarkTheme() switch here later
@Composable
fun ClockAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = ClockTypography,
        content = content
    )
}

//@Composable
//fun ClockAppTheme(
//    darkTheme: Boolean = true, // Default to dark theme for dark clock aesthetic
//    content: @Composable () -> Unit
//) {
//    val colorScheme = DarkColors
//
//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = ClockTypography,
//        content = content
//    )
//}