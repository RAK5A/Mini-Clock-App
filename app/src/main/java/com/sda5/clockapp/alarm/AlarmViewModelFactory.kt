package com.sda5.clockapp.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sda5.clockapp.ClockApplication

class AlarmViewModelFactory(private val app: ClockApplication) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AlarmViewModel(app.database.alarmDao(), app.alarmScheduler) as T
    }
}