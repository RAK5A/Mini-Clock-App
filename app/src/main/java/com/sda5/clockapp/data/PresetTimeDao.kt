package com.sda5.clockapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sda5.clockapp.model.PresetTime
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetTimeDao {
    @Query("SELECT * FROM preset_times ORDER BY sortOrder")
    fun getAll(): Flow<List<PresetTime>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(preset: PresetTime)

    @Query("DELETE FROM preset_times WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: Set<String>)
}