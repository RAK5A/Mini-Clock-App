package com.sda5.clockapp.alarm

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sda5.clockapp.ui.theme.AlarmAccent
import com.sda5.clockapp.ui.theme.AlarmGradientBrush
import com.sda5.clockapp.ui.theme.ClockBackground
import com.sda5.clockapp.ui.theme.ClockMutedText
import com.sda5.clockapp.ui.theme.ClockSurfaceElevated
import com.sda5.clockapp.ui.theme.GlassBorder
import com.sda5.clockapp.ui.theme.GlassSurface

data class AlarmItem(
    val id: String,
    val time: String,
    val amPm: String,
    val label: String,
    val repeatDays: String,
    val isEnabled: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(modifier: Modifier = Modifier) {
    val alarms = remember {
        mutableStateListOf(
            AlarmItem("1", "06:30", "AM", "Morning Run", "Mon, Tue, Wed, Thu, Fri", true),
            AlarmItem("2", "07:45", "AM", "Work Shift", "Mon - Fri", true),
            AlarmItem("3", "09:00", "AM", "Weekend Wake-up", "Sat, Sun", false),
            AlarmItem("4", "10:30", "PM", "Bedtime Wind Down", "Everyday", true)
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ClockBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Alarm",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ClockBackground
                ),
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Alarm",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    alarms.add(
                        AlarmItem(
                            id = System.currentTimeMillis().toString(),
                            time = "08:00",
                            amPm = "AM",
                            label = "New Alarm",
                            repeatDays = "Mon - Fri",
                            isEnabled = true
                        )
                    )
                },
                containerColor = AlarmAccent,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Alarm",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Next Alarm Banner
            item {
                NextAlarmBanner()
            }

            item {
                Text(
                    text = "YOUR ALARMS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ClockMutedText,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            items(alarms, key = { it.id }) { alarm ->
                AlarmCard(
                    alarm = alarm,
                    onToggle = { isChecked ->
                        val index = alarms.indexOfFirst { it.id == alarm.id }
                        if (index != -1) {
                            alarms[index] = alarms[index].copy(isEnabled = isChecked)
                        }
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun NextAlarmBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(AlarmGradientBrush)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Next Alarm",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "In 8 hours 15 minutes",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tomorrow at 06:30 AM",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun AlarmCard(
    alarm: AlarmItem,
    onToggle: (Boolean) -> Unit
) {
    val cardAlpha = if (alarm.isEnabled) 1f else 0.5f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(ClockSurfaceElevated)
            .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = alarm.time,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = cardAlpha)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = alarm.amPm,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AlarmAccent.copy(alpha = cardAlpha),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = alarm.label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = cardAlpha * 0.9f)
            )

            Text(
                text = alarm.repeatDays,
                fontSize = 13.sp,
                color = ClockMutedText.copy(alpha = cardAlpha),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        Switch(
            checked = alarm.isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AlarmAccent,
                uncheckedThumbColor = ClockMutedText,
                uncheckedTrackColor = GlassSurface
            )
        )
    }
}