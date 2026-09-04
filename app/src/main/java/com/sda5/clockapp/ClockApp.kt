package com.sda5.clockapp

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sda5.clockapp.alarm.AlarmScreen
import com.sda5.clockapp.navigation.ClockDestination
import com.sda5.clockapp.stopwatch.StopwatchScreen
import com.sda5.clockapp.timer.TimerScreen
import com.sda5.clockapp.ui.theme.AlarmAccent
import com.sda5.clockapp.ui.theme.ClockBackground
import com.sda5.clockapp.ui.theme.ClockMutedText
import com.sda5.clockapp.ui.theme.ClockPrimary
import com.sda5.clockapp.ui.theme.ClockSurfaceElevated
import com.sda5.clockapp.ui.theme.GlassBorder
import com.sda5.clockapp.ui.theme.GlassSurface
import com.sda5.clockapp.ui.theme.StopwatchAccent
import com.sda5.clockapp.ui.theme.WorldClockAccent
import com.sda5.clockapp.worldclock.WorldClockScreen

@Composable
fun ClockApp() {
    val navController = rememberNavController()

    Scaffold(
        containerColor = ClockBackground,
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

                val activeColor = when (destination) {
                    ClockDestination.Alarm -> AlarmAccent
                    ClockDestination.WorldClock -> WorldClockAccent
                    ClockDestination.Stopwatch -> StopwatchAccent
                    ClockDestination.Timer -> ClockPrimary
                }

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
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
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