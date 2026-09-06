package com.sda5.clockapp.alarm

import com.sda5.clockapp.model.Alarm
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

fun nextTrigger(alarm: Alarm, now: LocalDateTime = LocalDateTime.now()): LocalDateTime? {
    val alarmTime = LocalTime.of(alarm.hour, alarm.minute)

    if (alarm.repeatDays.isEmpty()) {
        var candidate = now.toLocalDate().atTime(alarmTime)
        if (!candidate.isAfter(now)) candidate = candidate.plusDays(1)
        return candidate
    }

    return (0..7)
        .map { offset -> now.toLocalDate().plusDays(offset.toLong()) }
        .firstOrNull { date ->
            alarm.repeatDays.contains(date.dayOfWeek) && date.atTime(alarmTime).isAfter(now)
        }
        ?.atTime(alarmTime)
}

fun countdownText(now: LocalDateTime, target: LocalDateTime): Pair<Long, Long> {
    val duration = Duration.between(now, target)
    return duration.toHours() to (duration.toMinutes() % 60)
}

fun nextOccurrenceLabel(alarm: Alarm, now: LocalDateTime = LocalDateTime.now()): String {
    val next = nextTrigger(alarm, now) ?: return ""
    val dayPrefix = when (next.toLocalDate()) {
        now.toLocalDate() -> "Today"
        now.toLocalDate().plusDays(1) -> "Tomorrow"
        else -> ""
    }
    val weekday = next.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val date = next.format(DateTimeFormatter.ofPattern("MMM d"))
    val time = next.format(DateTimeFormatter.ofPattern("h:mm a"))
    val prefix = if (dayPrefix.isNotEmpty()) "$dayPrefix-$weekday, $date" else "$weekday, $date"
    return "$prefix, $time"
}