package com.sda5.clockapp.stopwatch

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StopwatchViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(StopwatchRunState())
    val uiState: StateFlow<StopwatchRunState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            StopwatchState.uiState.collect { runState ->
                _uiState.update { runState }
            }
        }
    }

    fun startStopwatch() {
        val context = getApplication<Application>()
        val intent = Intent(context, StopwatchService::class.java).apply {
            action = StopwatchService.ACTION_START
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun pauseStopwatch() = sendCommand(StopwatchService.ACTION_PAUSE)

    fun resumeStopwatch() = sendCommand(StopwatchService.ACTION_RESUME)

    fun lap() = sendCommand(StopwatchService.ACTION_LAP)

    fun resetStopwatch() {
        val context = getApplication<Application>()
        val intent = Intent(context, StopwatchService::class.java).apply {
            action = StopwatchService.ACTION_RESET
        }
        context.startService(intent)
    }

    private fun sendCommand(action: String) {
        val context = getApplication<Application>()
        context.startService(Intent(context, StopwatchService::class.java).setAction(action))
    }
}
