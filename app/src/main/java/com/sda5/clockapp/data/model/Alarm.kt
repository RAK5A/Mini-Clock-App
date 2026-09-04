package com.sda5.clockapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey val id: Long,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val repeatDays: Set<DayOfWeek> = emptySet(), // empty = one-time alarm
    val soundEnabled: Boolean = true,
    val soundUri: String? = null, // null = use the system default alarm sound
    val vibrationEnabled: Boolean = true,
    val snoozeEnabled: Boolean = true,
    val isEnabled: Boolean = true,
)