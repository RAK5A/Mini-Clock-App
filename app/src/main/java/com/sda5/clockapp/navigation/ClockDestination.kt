package com.sda5.clockapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.graphics.vector.ImageVector

/* Bottom Navigations for our project */
sealed class ClockDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Alarm : ClockDestination(
        route = "alarm",
        label = "Alarm",
        selectedIcon = Icons.Filled.Alarm,
        unselectedIcon = Icons.Outlined.Alarm
    )

    object WorldClock : ClockDestination(
        route = "world_clock",
        label = "World clock",
        selectedIcon = Icons.Filled.Public,
        unselectedIcon = Icons.Outlined.Public
    )

    object Stopwatch : ClockDestination(
        route = "stopwatch",
        label = "Stopwatch",
        selectedIcon = Icons.Filled.Timer,
        unselectedIcon = Icons.Outlined.Timer
    )

    object Timer : ClockDestination(
        route = "timer",
        label = "Timer",
        selectedIcon = Icons.Filled.HourglassBottom,
        unselectedIcon = Icons.Outlined.HourglassBottom
    )

    companion object {
        val items = listOf(Alarm, WorldClock, Stopwatch, Timer)
    }
}