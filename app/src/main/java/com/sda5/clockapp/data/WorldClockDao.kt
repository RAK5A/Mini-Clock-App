package com.sda5.clockapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sda5.clockapp.model.WorldClockCity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorldClockDao {
    @Query("SELECT * FROM world_clock_cities ORDER BY sortOrder, displayName")
    fun getAll(): Flow<List<WorldClockCity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(city: WorldClockCity)

    @Delete
    suspend fun delete(city: WorldClockCity)
}