package com.sda5.clockapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sda5.clockapp.data.AlarmDao
import com.sda5.clockapp.model.Alarm
import com.sda5.clockapp.model.WorldClockCity

@Database(entities = [Alarm::class, WorldClockCity::class], version = 4)
@TypeConverters(DayOfWeekSetConverter::class)
abstract class ClockDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun worldClockDao(): WorldClockDao

    companion object {
        @Volatile private var instance: ClockDatabase? = null

        fun getInstance(context: Context): ClockDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ClockDatabase::class.java,
                    "clock_app.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
    }
}