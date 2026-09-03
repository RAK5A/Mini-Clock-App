package com.sda5.clockapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sda5.clockapp.data.model.Alarm
import com.sda5.clockapp.data.alarms.AlarmDao

@Database(entities = [Alarm::class], version = 1)
@TypeConverters(DayOfWeekSetConverter::class)
abstract class ClockDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile private var instance: ClockDatabase? = null

        fun getInstance(context: Context): ClockDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ClockDatabase::class.java,
                    "clock_app.db"
                ).build().also { instance = it }
            }
    }
}