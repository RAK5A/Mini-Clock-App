package com.sda5.clockapp.stopwatch

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.StateFlow

class StopwatchViewModel(application: Application) : AndroidViewModel(application) {

    val uiState: StateFlow<StopwatchRunState> = StopwatchState.uiState

    fun toggleStartStop() {
        val context = getApplication<Application>()
        val intent = Intent(context, StopwatchService::class.java)
        if (uiState.value.isRunning) {
            intent.action = StopwatchService.ACTION_PAUSE
            context.startService(intent)
        } else {
            intent.action = StopwatchService.ACTION_START
            ContextCompat.startForegroundService(context, intent)
        }
    }

    fun addLap() {
        val context = getApplication<Application>()
        context.startService(Intent(context, StopwatchService::class.java).setAction(StopwatchService.ACTION_LAP))
    }

    fun restart() {
        val context = getApplication<Application>()
        context.stopService(Intent(context, StopwatchService::class.java))
    }
}