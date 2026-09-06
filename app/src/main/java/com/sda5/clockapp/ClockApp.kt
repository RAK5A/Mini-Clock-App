package com.sda5.clockapp

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.sda5.clockapp.alarm.AlarmScreen
import com.sda5.clockapp.alarm.AlarmViewModel
import com.sda5.clockapp.alarm.AlarmViewModelFactory
import com.sda5.clockapp.navigation.ClockDestination
import com.sda5.clockapp.stopwatch.StopwatchScreen
import com.sda5.clockapp.timer.TimerScreen
import com.sda5.clockapp.worldclock.WorldClockScreen

private const val ALARM_EDIT_ROUTE = "alarm_edit?alarmId={alarmId}"

@Composable
fun ClockApp() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as ClockApplication
    val alarmViewModel: AlarmViewModel = viewModel(factory = AlarmViewModelFactory(application))

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* alarms still schedule either way */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val showNavBar = backStackEntry?.destination?.route != ALARM_EDIT_ROUTE

    Scaffold(
        bottomBar = {
            if (showNavBar) {
                ClockPillNavBar(navController, backStackEntry?.destination)
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
                arguments = listOf(navArgument("alarmId") { type = NavType.LongType; defaultValue = -1L })
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
private fun ClockPillNavBar(navController: NavHostController, currentDestination: NavDestination?) {
    val items = ClockDestination.items
    val selectedIndex = items.indexOfFirst { destination ->
        currentDestination?.hierarchy?.any { it.route == destination.route } == true
    }.coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(6.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val segmentWidth = maxWidth / items.size
            val indicatorOffset by animateDpAsState(
                targetValue = segmentWidth * selectedIndex,
                label = "navIndicator"
            )

            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surface)
            )

            Row(modifier = Modifier.fillMaxSize()) {
                items.forEachIndexed { index, destination ->
                    val selected = index == selectedIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = destination.label,
                            color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}