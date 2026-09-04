package com.sda5.clockapp

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.sda5.clockapp.ui.theme.ClockBackground
import com.sda5.clockapp.ui.theme.ClockMutedText
import com.sda5.clockapp.ui.theme.ClockPrimary
import com.sda5.clockapp.ui.theme.ClockSurfaceElevated
import com.sda5.clockapp.ui.theme.GlassBorder
import com.sda5.clockapp.worldclock.WorldClockScreen

private const val ALARM_EDIT_ROUTE = "alarm_edit?alarmId={alarmId}"

@Composable
fun ClockApp() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as ClockApplication
    val alarmViewModel: AlarmViewModel = viewModel(factory = AlarmViewModelFactory(application))

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* alarms still schedule either way — this only affects the ringing notification's visibility */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val showBottomBar = backStackEntry?.destination?.route != ALARM_EDIT_ROUTE

    Scaffold(
        containerColor = ClockBackground,
        bottomBar = {
            if (showBottomBar) {
                ClockBottomNavBar(navController, backStackEntry?.destination)
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
private fun ClockBottomNavBar(navController: NavHostController, currentDestination: NavDestination?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(34.dp))
                .background(ClockSurfaceElevated)
                .border(1.dp, GlassBorder, RoundedCornerShape(34.dp))
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClockDestination.items.forEach { destination ->
                val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
                val activeColor = ClockPrimary

                val iconColor by animateColorAsState(
                    targetValue = if (selected) activeColor else ClockMutedText,
                    animationSpec = tween(200),
                    label = "NavIconColor"
                )

                val pillBgColor by animateColorAsState(
                    targetValue = if (selected) activeColor.copy(alpha = 0.15f) else Color.Transparent,
                    animationSpec = tween(200),
                    label = "NavPillBg"
                )

                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(pillBgColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                            contentDescription = destination.label,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = destination.label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = iconColor,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
