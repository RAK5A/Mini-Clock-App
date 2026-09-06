package com.sda5.clockapp.timer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class TimerRunState(
    val status: TimerStatus = TimerStatus.SETUP,
    val totalSeconds: Long = 0L,
    val remainingMillis: Long = 0L,
    val targetFinishTime: String = ""
)

// Shared between TimerService (writer) and TimerViewModel (reader) so the running
// countdown has a home that outlives any single ViewModel/Activity instance.
object TimerState {
    private val _uiState = MutableStateFlow(TimerRunState())
    val uiState: StateFlow<TimerRunState> = _uiState.asStateFlow()

    fun update(transform: (TimerRunState) -> TimerRunState) {
        _uiState.value = transform(_uiState.value)
    }
}