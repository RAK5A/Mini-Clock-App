package com.sda5.clockapp.alarm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sda5.clockapp.model.Alarm
import java.time.DayOfWeek

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    alarmId: Long?,
    alarmViewModel: AlarmViewModel,
    onDone: () -> Unit
) {
    val existingAlarm = remember(alarmId) {
        alarmId?.let { id -> alarmViewModel.alarms.value.find { it.id == id } }
    }

    val timePickerState = rememberTimePickerState(
        initialHour = existingAlarm?.hour ?: 11,
        initialMinute = existingAlarm?.minute ?: 0,
        is24Hour = false
    )

    var label by rememberSaveable { mutableStateOf(existingAlarm?.label ?: "") }
    var repeatDays by rememberSaveable {
        mutableStateOf(existingAlarm?.repeatDays ?: emptySet<DayOfWeek>())
    }
    var soundEnabled by rememberSaveable { mutableStateOf(existingAlarm?.soundEnabled ?: true) }
    var vibrationEnabled by rememberSaveable { mutableStateOf(existingAlarm?.vibrationEnabled ?: true) }
    var snoozeEnabled by rememberSaveable { mutableStateOf(existingAlarm?.snoozeEnabled ?: true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TimePicker(state = timePickerState)

        Spacer(modifier = Modifier.height(16.dp))

        val previewAlarm = Alarm(
            id = existingAlarm?.id ?: 0L,
            hour = timePickerState.hour,
            minute = timePickerState.minute,
            repeatDays = repeatDays
        )
        Text(
            text = nextOccurrenceLabel(previewAlarm),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val days = listOf(
                DayOfWeek.SUNDAY to "S",
                DayOfWeek.MONDAY to "M",
                DayOfWeek.TUESDAY to "T",
                DayOfWeek.WEDNESDAY to "W",
                DayOfWeek.THURSDAY to "T",
                DayOfWeek.FRIDAY to "F",
                DayOfWeek.SATURDAY to "S"
            )
            days.forEach { (day, letter) ->
                FilterChip(
                    selected = repeatDays.contains(day),
                    onClick = {
                        repeatDays = if (repeatDays.contains(day)) {
                            repeatDays - day
                        } else {
                            repeatDays + day
                        }
                    },
                    label = { Text(letter) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Alarm name") },
            modifier = Modifier.fillMaxWidth()
        )

        /*Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()

        SettingsToggleRow(
            title = "Alarm sound",
            subtitle = "Over the Horizon",
            checked = soundEnabled,
            onCheckedChange = { soundEnabled = it }
        )
        HorizontalDivider()

        SettingsToggleRow(
            title = "Vibration",
            subtitle = "Basic call",
            checked = vibrationEnabled,
            onCheckedChange = { vibrationEnabled = it }
        )
        HorizontalDivider()*/

        SettingsToggleRow(
            title = "Snooze",
            subtitle = "5 minutes, 3 times",
            checked = snoozeEnabled,
            onCheckedChange = { snoozeEnabled = it }
        )
        HorizontalDivider()

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onDone) {
                Text("Cancel")
            }

            if (existingAlarm != null) {
                TextButton(onClick = {
                    alarmViewModel.deleteAlarm(existingAlarm.id)
                    onDone()
                }) {
                    Text("Delete")
                }
            }

            Button(onClick = {
                val alarm = Alarm(
                    id = existingAlarm?.id ?: System.currentTimeMillis(),
                    hour = timePickerState.hour,
                    minute = timePickerState.minute,
                    label = label,
                    repeatDays = repeatDays,
                    soundEnabled = soundEnabled,
                    vibrationEnabled = vibrationEnabled,
                    snoozeEnabled = snoozeEnabled,
                    isEnabled = true
                )
                if (existingAlarm != null) {
                    alarmViewModel.updateAlarm(alarm)
                } else {
                    alarmViewModel.addAlarm(alarm)
                }
                onDone()
            }) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}