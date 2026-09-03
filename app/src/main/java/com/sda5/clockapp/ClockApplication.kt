package com.sda5.clockapp

import android.app.Application
import com.sda5.clockapp.alarm.AlarmScheduler
import com.sda5.clockapp.alarm.NotificationHelper
import com.sda5.clockapp.data.ClockDatabase

class ClockApplication : Application() {
    val database: ClockDatabase by lazy { ClockDatabase.getInstance(this) }
    val alarmScheduler: AlarmScheduler by lazy { AlarmScheduler(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}