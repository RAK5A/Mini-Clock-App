package com.sda5.clockapp.worldclock

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

fun formattedCityName(zoneId: String): String =
    zoneId.substringAfterLast('/').replace('_', ' ')

fun formattedRegion(zoneId: String): String =
    zoneId.substringBeforeLast('/', missingDelimiterValue = "").replace('_', ' ')

data class CityTimeInfo(
    val timeText: String,
    val dayLabel: String,
    val utcOffsetLabel: String
)

fun cityTimeInfo(zoneId: String, now: ZonedDateTime): CityTimeInfo {
    val cityNow = now.withZoneSameInstant(ZoneId.of(zoneId))
    val timeText = cityNow.format(DateTimeFormatter.ofPattern("h:mm a"))

    val dayLabel = when {
        cityNow.toLocalDate().isBefore(now.toLocalDate()) -> "Yesterday"
        cityNow.toLocalDate().isAfter(now.toLocalDate()) -> "Tomorrow"
        else -> cityNow.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    val totalOffsetSeconds = cityNow.offset.totalSeconds
    val offsetHours = totalOffsetSeconds / 3600
    val offsetMinutes = kotlin.math.abs((totalOffsetSeconds % 3600) / 60)
    val sign = if (offsetHours >= 0) "+" else "-"
    val utcOffsetLabel = if (offsetMinutes == 0) {
        "UTC$sign${kotlin.math.abs(offsetHours)}"
    } else {
        "UTC$sign${kotlin.math.abs(offsetHours)}:${offsetMinutes.toString().padStart(2, '0')}"
    }

    return CityTimeInfo(timeText, dayLabel, utcOffsetLabel)
}