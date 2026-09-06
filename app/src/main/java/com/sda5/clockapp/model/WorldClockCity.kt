package com.sda5.clockapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "world_clock_cities")
data class WorldClockCity(
    @PrimaryKey val id: Long,
    val zoneId: String,
    val displayName: String,
    val sortOrder: Int = 0
)