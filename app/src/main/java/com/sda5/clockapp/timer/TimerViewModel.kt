package com.sda5.clockapp.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PresetTime(
    val id: String = java.util.UUID.randomUUID().toString(),
    val hours: Int,
    val minutes: Int,
    val seconds: Int
) {
    val totalSeconds: Long
        get() = hours * 3600L + minutes * 60L + seconds.toLong()

    val formattedString: String
        get() = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}

enum class TimerStatus {
    SETUP,
    RUNNING,
    PAUSED,
    FINISHED
}

data class TimerUiState(
    val hours: Int = 0,
    val minutes: Int = 0,
    val seconds: Int = 0,
    val presets: List<PresetTime> = listOf(
        PresetTime(hours = 0, minutes = 10, seconds = 0),
        PresetTime(hours = 0, minutes = 5, seconds = 0),
        PresetTime(hours = 0, minutes = 1, seconds = 0)
    ),
    val status: TimerStatus = TimerStatus.SETUP,
    val totalSeconds: Long = 0L,
    val remainingMillis: Long = 0L,
    val targetFinishTime: String = "",
    val showLiveNotification: Boolean = true
) {
    val isStartEnabled: Boolean
        get() = hours > 0 || minutes > 0 || seconds > 0

    val remainingSeconds: Long
        get() = (remainingMillis + 999L) / 1000L

    val remainingFraction: Float
        get() = if (totalSeconds > 0) (remainingMillis.toFloat() / (totalSeconds * 1000f)).coerceIn(0f, 1f) else 0f

    val totalDurationDisplay: String
        get() {
            val h = totalSeconds / 3600
            val m = (totalSeconds % 3600) / 60
            val s = totalSeconds % 60
            return buildString {
                if (h > 0) append("${h} h ")
                if (m > 0) append("${m} m ")
                if (s > 0 || (h == 0L && m == 0L)) append("${s} s")
            }.trim()
        }

    val remainingDisplay: String
        get() {
            val remSec = remainingSeconds
            val h = remSec / 3600
            val m = (remSec % 3600) / 60
            val s = remSec % 60

            return when {
                h > 0 -> String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s)
                m > 0 -> String.format(Locale.getDefault(), "%d:%02d", m, s)
                else -> "${s} s"
            }
        }

    val liveNotificationDisplay: String
        get() {
            val remSec = remainingSeconds
            val h = remSec / 3600
            val m = (remSec % 3600) / 60
            val s = remSec % 60
            return if (h > 0) {
                String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
            } else {
                String.format(Locale.getDefault(), "%02d:%02d", m, s)
            }
        }
}

class TimerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun setHours(hours: Int) {
        if (_uiState.value.status == TimerStatus.SETUP) {
            _uiState.update { it.copy(hours = hours.coerceIn(0, 99)) }
        }
    }

    fun setMinutes(minutes: Int) {
        if (_uiState.value.status == TimerStatus.SETUP) {
            _uiState.update { it.copy(minutes = minutes.coerceIn(0, 59)) }
        }
    }

    fun setSeconds(seconds: Int) {
        if (_uiState.value.status == TimerStatus.SETUP) {
            _uiState.update { it.copy(seconds = seconds.coerceIn(0, 59)) }
        }
    }

    fun applyPreset(preset: PresetTime) {
        if (_uiState.value.status == TimerStatus.SETUP) {
            _uiState.update {
                it.copy(
                    hours = preset.hours,
                    minutes = preset.minutes,
                    seconds = preset.seconds
                )
            }
        }
    }

    fun addPreset(hours: Int, minutes: Int, seconds: Int) {
        if (hours == 0 && minutes == 0 && seconds == 0) return
        val newPreset = PresetTime(hours = hours, minutes = minutes, seconds = seconds)
        _uiState.update { state ->
            if (state.presets.none { it.formattedString == newPreset.formattedString }) {
                state.copy(presets = state.presets + newPreset)
            } else state
        }
    }

    fun deletePreset(preset: PresetTime) {
        _uiState.update { state ->
            state.copy(presets = state.presets.filterNot { it.id == preset.id })
        }
    }

    fun startTimer() {
        val current = _uiState.value
        val totalSec = current.hours * 3600L + current.minutes * 60L + current.seconds.toLong()
        if (totalSec <= 0L) return

        val finishMillis = System.currentTimeMillis() + (totalSec * 1000L)
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        val finishTimeStr = sdf.format(Date(finishMillis))

        _uiState.update {
            it.copy(
                status = TimerStatus.RUNNING,
                totalSeconds = totalSec,
                remainingMillis = totalSec * 1000L,
                targetFinishTime = finishTimeStr
            )
        }

        runTimerLoop()
    }

    fun pauseTimer() {
        if (_uiState.value.status == TimerStatus.RUNNING) {
            timerJob?.cancel()
            _uiState.update { it.copy(status = TimerStatus.PAUSED) }
        }
    }

    fun resumeTimer() {
        if (_uiState.value.status == TimerStatus.PAUSED) {
            val remainingSec = (_uiState.value.remainingMillis + 999L) / 1000L
            val finishMillis = System.currentTimeMillis() + (remainingSec * 1000L)
            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
            val finishTimeStr = sdf.format(Date(finishMillis))

            _uiState.update {
                it.copy(
                    status = TimerStatus.RUNNING,
                    targetFinishTime = finishTimeStr
                )
            }
            runTimerLoop()
        }
    }

    fun deleteTimer() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                status = TimerStatus.SETUP,
                totalSeconds = 0L,
                remainingMillis = 0L,
                targetFinishTime = ""
            )
        }
    }

    fun toggleLiveNotification() {
        _uiState.update { it.copy(showLiveNotification = !it.showLiveNotification) }
    }

    private fun runTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val initialMillis = _uiState.value.remainingMillis

            while (_uiState.value.status == TimerStatus.RUNNING) {
                val elapsed = System.currentTimeMillis() - startTime
                val currentRemaining = (initialMillis - elapsed).coerceAtLeast(0L)

                _uiState.update { it.copy(remainingMillis = currentRemaining) }

                if (currentRemaining <= 0L) {
                    _uiState.update { it.copy(status = TimerStatus.FINISHED) }
                    break
                }
                delay(50L)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
