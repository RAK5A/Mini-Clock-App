package com.sda5.clockapp

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sda5.clockapp.navigation.ClockDestination
import com.sda5.clockapp.screens.AlarmScreen
import com.sda5.clockapp.screens.StopwatchScreen
import com.sda5.clockapp.screens.TimerScreen
import com.sda5.clockapp.screens.WorldClockScreen

@Composable
fun ClockApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { ClockBottomNavBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ClockDestination.Alarm.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ClockDestination.Alarm.route) { AlarmScreen() }
            composable(ClockDestination.WorldClock.route) { WorldClockScreen() }
            composable(ClockDestination.Stopwatch.route) { StopwatchScreen() }
            composable(ClockDestination.Timer.route) { TimerScreen() }
        }
    }
}

@Composable
private fun ClockBottomNavBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar {
        ClockDestination.items.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = destination.label
                    )
                },
                label = { Text(destination.label) }
            )
        }
    }
}