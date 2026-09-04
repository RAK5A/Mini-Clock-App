package com.sda5.clockapp.data

import androidx.room.TypeConverter
import java.time.DayOfWeek

class DayOfWeekSetConverter {
    @TypeConverter
    fun fromSet(days: Set<DayOfWeek>): String =
        days.joinToString(",") { it.value.toString() }

    @TypeConverter
    fun toSet(raw: String): Set<DayOfWeek> =
        if (raw.isBlank()) emptySet()
        else raw.split(",").map { DayOfWeek.of(it.toInt()) }.toSet()
}