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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sda5.clockapp.alarm.AlarmEditScreen
import com.sda5.clockapp.navigation.ClockDestination
import com.sda5.clockapp.alarm.AlarmScreen
import com.sda5.clockapp.alarm.AlarmViewModel
import com.sda5.clockapp.stopwatch.StopwatchScreen
import com.sda5.clockapp.timer.TimerScreen
import com.sda5.clockapp.worldclock.WorldClockScreen

private const val ALARM_EDIT_ROUTE = "alarm_edit?alarmId={alarmId}"

@Composable
fun ClockApp() {
    val navController = rememberNavController()
    val alarmViewModel: AlarmViewModel = viewModel()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute != ALARM_EDIT_ROUTE

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                ClockBottomNavBar(
                    navController = navController,
                    currentDestination = backStackEntry?.destination
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ClockDestination.Alarm.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ClockDestination.Alarm.route) {
                AlarmScreen(
                    alarmViewModel = alarmViewModel,
                    onAddAlarm = { navController.navigate("alarm_edit") },
                    onEditAlarm = { id -> navController.navigate("alarm_edit?alarmId=$id") }
                )
            }
            composable(ClockDestination.WorldClock.route) { WorldClockScreen() }
            composable(ClockDestination.Stopwatch.route) { StopwatchScreen() }
            composable(ClockDestination.Timer.route) { TimerScreen() }

            composable(
                route = ALARM_EDIT_ROUTE,
                arguments = listOf(
                    navArgument("alarmId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStack ->
                val alarmId = backStack.arguments?.getLong("alarmId") ?: -1L
                AlarmEditScreen(
                    alarmId = if (alarmId == -1L) null else alarmId,
                    alarmViewModel = alarmViewModel,
                    onDone = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun ClockBottomNavBar(
    navController: NavHostController,
    currentDestination: NavDestination?
) {
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