package com.sda5.clockapp.stopwatch

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class StopwatchStatus {
    IDLE,
    RUNNING,
    PAUSED
}

data class LapTableEntry(
    val lapNumber: Int,
    val lapDurationMs: Long,
    val overallTimeMs: Long
)

data class StopwatchRunState(
    val status: StopwatchStatus = StopwatchStatus.IDLE,
    val elapsedTimeMs: Long = 0L,
    val laps: List<LapTableEntry> = emptyList()
)

// Shared between StopwatchService (writer) and StopwatchViewModel (reader) so the running
// stopwatch survives when the app closes or is backgrounded.
object StopwatchState {
    private val _uiState = MutableStateFlow(StopwatchRunState())
    val uiState: StateFlow<StopwatchRunState> = _uiState.asStateFlow()

    fun update(transform: (StopwatchRunState) -> StopwatchRunState) {
        _uiState.value = transform(_uiState.value)
    }
}
