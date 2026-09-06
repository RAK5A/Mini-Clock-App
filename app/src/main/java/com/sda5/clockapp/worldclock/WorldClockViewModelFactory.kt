package com.sda5.clockapp.worldclock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sda5.clockapp.ClockApplication

class WorldClockViewModelFactory(private val app: ClockApplication) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WorldClockViewModel(app.database.worldClockDao()) as T
    }
}