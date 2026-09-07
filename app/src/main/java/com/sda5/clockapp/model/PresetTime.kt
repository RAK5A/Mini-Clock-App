package com.sda5.clockapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale
import java.util.UUID

@Entity(tableName = "preset_times")
data class PresetTime(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val sortOrder: Int = 0
) {
    val totalSeconds: Long
        get() = hours * 3600L + minutes * 60L + seconds.toLong()

    val formattedString: String
        get() = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}