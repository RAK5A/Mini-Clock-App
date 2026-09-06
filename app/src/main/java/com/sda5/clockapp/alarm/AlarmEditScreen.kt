package com.sda5.clockapp.alarm

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.sda5.clockapp.data.ClockDatabase
import com.sda5.clockapp.model.Alarm
import com.sda5.clockapp.ui.components.AlarmTimeWheelPicker
import com.sda5.clockapp.ui.theme.ClockAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale

private fun hour24To12(hour24: Int): Pair<Int, Boolean> {
    val isAm = hour24 < 12
    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
    return hour12 to isAm
}

private fun hour12To24(hour12: Int, isAm: Boolean): Int = when {
    isAm && hour12 == 12 -> 0
    !isAm && hour12 != 12 -> hour12 + 12
    else -> hour12
}

private val WEEKDAYS = setOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY
)
private val WEEKEND = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

private fun repeatPresetLabel(days: Set<DayOfWeek>): String? = when {
    days.isEmpty() -> "Once"
    days.size == 7 -> "Every day"
    days == WEEKDAYS -> "Weekdays"
    days == WEEKEND -> "Weekend"
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditScreen(
    alarmId: Long?,
    alarmViewModel: AlarmViewModel,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val existingAlarm = remember(alarmId) {
        alarmId?.let { id -> alarmViewModel.alarms.value.find { it.id == id } }
    }

    val (initialHour12, initialIsAm) = hour24To12(existingAlarm?.hour ?: 7)
    var hour12 by rememberSaveable { mutableStateOf(initialHour12) }
    var minute by rememberSaveable { mutableStateOf(existingAlarm?.minute ?: 30) }
    var isAm by rememberSaveable { mutableStateOf(initialIsAm) }

    var label by rememberSaveable { mutableStateOf(existingAlarm?.label ?: "") }
    var repeatDays by rememberSaveable {
        mutableStateOf(existingAlarm?.repeatDays ?: emptySet<DayOfWeek>())
    }
    var soundEnabled by rememberSaveable { mutableStateOf(existingAlarm?.soundEnabled ?: true) }
    var soundUri by rememberSaveable { mutableStateOf(existingAlarm?.soundUri) }
    var vibrationEnabled by rememberSaveable { mutableStateOf(existingAlarm?.vibrationEnabled ?: true) }
    var snoozeEnabled by rememberSaveable { mutableStateOf(existingAlarm?.snoozeEnabled ?: true) }
    var snoozeIntervalMinutes by rememberSaveable { mutableStateOf(existingAlarm?.snoozeIntervalMinutes ?: 5) }
    var snoozeRepeatLimit by rememberSaveable {
        mutableStateOf<Int?>(if (existingAlarm != null) existingAlarm.snoozeRepeatLimit else 3)
    }

    val intervalPresets = listOf(5, 10, 15, 20)
    var isCustomInterval by rememberSaveable { mutableStateOf(snoozeIntervalMinutes !in intervalPresets) }
    var customIntervalText by rememberSaveable {
        mutableStateOf(if (snoozeIntervalMinutes !in intervalPresets) snoozeIntervalMinutes.toString() else "")
    }
    val repeatPresets = listOf(3, 5)

    val soundTitle by produceState(initialValue = "Default alarm sound", soundUri) {
        value = withContext(Dispatchers.IO) {
            val uri = soundUri?.let(Uri::parse)
                ?: RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
            uri?.let { RingtoneManager.getRingtone(context, it)?.getTitle(context) } ?: "Default alarm sound"
        }
    }

    val snoozeSubtitle = if (snoozeRepeatLimit == null) {
        "$snoozeIntervalMinutes minutes, Forever"
    } else {
        "$snoozeIntervalMinutes minutes, $snoozeRepeatLimit times"
    }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        @Suppress("DEPRECATION")
        val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        soundUri = uri?.toString()
    }

    fun openRingtonePicker() {
        val currentUri = soundUri?.let(Uri::parse)
            ?: RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Alarm sound")
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri)
        }
        ringtonePickerLauncher.launch(intent)
    }

    fun performSave() {
        val alarm = Alarm(
            id = existingAlarm?.id ?: System.currentTimeMillis(),
            hour = hour12To24(hour12, isAm),
            minute = minute,
            label = label,
            repeatDays = repeatDays,
            soundEnabled = soundEnabled,
            soundUri = soundUri,
            vibrationEnabled = vibrationEnabled,
            snoozeEnabled = snoozeEnabled,
            snoozeIntervalMinutes = snoozeIntervalMinutes,
            snoozeRepeatLimit = snoozeRepeatLimit,
            isEnabled = true,
        )
        if (existingAlarm != null) {
            alarmViewModel.updateAlarm(alarm)
        } else {
            alarmViewModel.addAlarm(alarm)
        }
        onDone()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDone) {
                Icon(Icons.Filled.Close, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                text = if (existingAlarm != null) "Edit Alarm" else "Add Alarm",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { performSave() }) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
//                .clip(RoundedCornerShape(24.dp))
//                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 12.dp)
        ) {
            AlarmTimeWheelPicker(
                hour12 = hour12,
                minute = minute,
                isAm = isAm,
                onHourChange = { hour12 = it },
                onMinuteChange = { minute = it },
                onPeriodChange = { isAm = it }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                RingInPill(hour24 = hour12To24(hour12, isAm), minute = minute, repeatDays = repeatDays)
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Repeat", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            repeatPresetLabel(repeatDays)?.let { presetLabel ->
                PresetBadge(text = presetLabel)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val days = listOf(
                DayOfWeek.SUNDAY to "S", DayOfWeek.MONDAY to "M", DayOfWeek.TUESDAY to "T",
                DayOfWeek.WEDNESDAY to "W", DayOfWeek.THURSDAY to "T", DayOfWeek.FRIDAY to "F",
                DayOfWeek.SATURDAY to "S"
            )
            days.forEach { (day, letter) ->
                DayToggle(
                    letter = letter,
                    selected = repeatDays.contains(day),
                    onClick = {
                        repeatDays = if (repeatDays.contains(day)) repeatDays - day else repeatDays + day
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text("Alarm name") },
            trailingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            )

        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp)
        ) {
            SettingsToggleRow(
                icon = Icons.Filled.MusicNote,
                title = "Alarm sound",
                subtitle = soundTitle,
                checked = soundEnabled,
                onCheckedChange = { soundEnabled = it },
                onLabelClick = { openRingtonePicker() }
            )
            HorizontalDivider()

            SettingsToggleRow(
                icon = Icons.Filled.Vibration,
                title = "Vibration",
                subtitle = "Vibrate when this alarm rings",
                checked = vibrationEnabled,
                onCheckedChange = { vibrationEnabled = it }
            )
            HorizontalDivider()

            SettingsToggleRow(
                icon = Icons.Filled.Alarm,
                title = "Snooze",
                subtitle = snoozeSubtitle,
                checked = snoozeEnabled,
                onCheckedChange = { snoozeEnabled = it }
            )

            AnimatedVisibility(
                visible = snoozeEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Interval",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            intervalPresets.forEach { minutes ->
                                CompactChip(
                                    text = "${minutes}m",
                                    selected = !isCustomInterval && snoozeIntervalMinutes == minutes,
                                    onClick = {
                                        isCustomInterval = false
                                        snoozeIntervalMinutes = minutes
                                    }
                                )
                            }
//                            CompactChip(
//                                text = "Custom",
//                                selected = isCustomInterval,
//                                onClick = { isCustomInterval = true }
//                            )
                        }
                        if (isCustomInterval) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = customIntervalText,
                                onValueChange = { text ->
                                    customIntervalText = text.filter { it.isDigit() }
                                    customIntervalText.toIntOrNull()?.let { snoozeIntervalMinutes = it }
                                },
                                label = { Text("Minutes") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Repeat limit",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            repeatPresets.forEach { times ->
                                CompactChip(
                                    text = "$times",
                                    selected = snoozeRepeatLimit == times,
                                    onClick = { snoozeRepeatLimit = times }
                                )
                            }
                            CompactChip(
                                text = "\u221E",
                                selected = snoozeRepeatLimit == null,
                                onClick = { snoozeRepeatLimit = null }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (existingAlarm != null) {
                TextButton(onClick = {
                    alarmViewModel.deleteAlarm(existingAlarm.id)
                    onDone()
                }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            OutlinedButton(onClick = onDone, shape = RoundedCornerShape(50)) {
                Text("Cancel")
            }

            Button(
                onClick = { performSave() },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun RingInPill(hour24: Int, minute: Int, repeatDays: Set<DayOfWeek>) {
    val previewAlarm = Alarm(id = 0L, hour = hour24, minute = minute, repeatDays = repeatDays)
    val now = LocalDateTime.now()
    val next = nextTrigger(previewAlarm, now) ?: return
    val (hours, minutes) = countdownText(now, next)
    val dayWord = when (next.toLocalDate()) {
        now.toLocalDate() -> null
        now.toLocalDate().plusDays(1) -> "Tomorrow"
        else -> next.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Schedule,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = buildAnnotatedString {
                append("Ring in ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("$hours hours $minutes minutes")
                }
                dayWord?.let { append(" ($it)") }
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PresetBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CompactChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
private fun DayToggle(letter: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onLabelClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onLabelClick != null) it.clickable(onClick = onLabelClick) else it }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Preview(showBackground = true)
@Composable
private fun AlarmEditScreenPreview() {
    val context = LocalContext.current
    val previewDatabase = remember {
        Room.inMemoryDatabaseBuilder(context, ClockDatabase::class.java).build()
    }
    val previewViewModel = remember {
        AlarmViewModel(previewDatabase.alarmDao(), AlarmScheduler(context))
    }

    ClockAppTheme {
        AlarmEditScreen(
            alarmId = null,
            alarmViewModel = previewViewModel,
            onDone = {}
        )
    }
}