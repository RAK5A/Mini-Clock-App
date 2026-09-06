package com.sda5.clockapp.stopwatch

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LapTableEntry(
    val lapNumber: Int,
    val lapDurationMs: Long,
    val overallTimeMs: Long
)

data class StopwatchRunState(
    val isRunning: Boolean = false,
    val elapsedMillis: Long = 0L,
    val laps: List<LapTableEntry> = emptyList()
)

// Shared between StopwatchService (writer) and StopwatchViewModel (reader) — same
// pattern as TimerState, so the running stopwatch survives the app closing.
object StopwatchState {
    private val _uiState = MutableStateFlow(StopwatchRunState())
    val uiState: StateFlow<StopwatchRunState> = _uiState.asStateFlow()

    fun update(transform: (StopwatchRunState) -> StopwatchRunState) {
        _uiState.value = transform(_uiState.value)
    }
}