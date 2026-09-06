package com.sda5.clockapp.timer

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

class TimerViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    init {
        // TimerService owns the running countdown so it survives the app closing —
        // this just mirrors whatever it reports into our own state for the UI.
        viewModelScope.launch {
            TimerState.uiState.collect { runState ->
                _uiState.update {
                    it.copy(
                        status = runState.status,
                        totalSeconds = runState.totalSeconds,
                        remainingMillis = runState.remainingMillis,
                        targetFinishTime = runState.targetFinishTime
                    )
                }
            }
        }
    }

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

        val context = getApplication<Application>()
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_TOTAL_SECONDS, totalSec)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun pauseTimer() = sendCommand(TimerService.ACTION_PAUSE)

    fun resumeTimer() = sendCommand(TimerService.ACTION_RESUME)

    fun deleteTimer() {
        val context = getApplication<Application>()
        context.stopService(Intent(context, TimerService::class.java))
    }

    private fun sendCommand(action: String) {
        val context = getApplication<Application>()
        context.startService(Intent(context, TimerService::class.java).setAction(action))
    }
}