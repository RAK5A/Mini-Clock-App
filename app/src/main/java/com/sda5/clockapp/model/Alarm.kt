package com.sda5.clockapp.model

import java.time.DayOfWeek

data class Alarm(
    val id: Long,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val repeatDays: Set<DayOfWeek> = emptySet(), // empty = one-time alarm
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val snoozeEnabled: Boolean = true,
    val isEnabled: Boolean = true,
)