package com.sda5.clockapp.timer

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sda5.clockapp.ClockApplication
import com.sda5.clockapp.data.PresetTimeDao
import com.sda5.clockapp.model.PresetTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

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
    val presets: List<PresetTime> = emptyList(),
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
    private val dao: PresetTimeDao = (application as ClockApplication).database.presetTimeDao()

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

        // Presets now live in Room, not local state — this keeps us mirrored to it.
        viewModelScope.launch {
            dao.getAll().collect { presets ->
                _uiState.update { it.copy(presets = presets) }
            }
        }

        // Seed the three defaults exactly once, ever — not "whenever the list is empty",
        // or deleting everything would just bring them back on next launch.
        viewModelScope.launch {
            val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            if (!prefs.getBoolean(KEY_PRESETS_SEEDED, false)) {
                listOf(
                    PresetTime(hours = 0, minutes = 10, seconds = 0, sortOrder = 0),
                    PresetTime(hours = 0, minutes = 5, seconds = 0, sortOrder = 1),
                    PresetTime(hours = 0, minutes = 1, seconds = 0, sortOrder = 2)
                ).forEach { dao.upsert(it) }
                prefs.edit().putBoolean(KEY_PRESETS_SEEDED, true).apply()
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
                it.copy(hours = preset.hours, minutes = preset.minutes, seconds = preset.seconds)
            }
        }
    }

    fun addPreset(hours: Int, minutes: Int, seconds: Int) {
        if (hours == 0 && minutes == 0 && seconds == 0) return
        viewModelScope.launch {
            val existing = _uiState.value.presets
            val newPreset = PresetTime(hours = hours, minutes = minutes, seconds = seconds, sortOrder = existing.size)
            if (existing.none { it.formattedString == newPreset.formattedString }) {
                dao.upsert(newPreset)
            }
        }
    }

    fun deletePresets(ids: Set<String>) {
        viewModelScope.launch {
            dao.deleteByIds(ids)
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

    fun toggleLiveNotification() {
        _uiState.update { it.copy(showLiveNotification = !it.showLiveNotification) }
    }

    private fun sendCommand(action: String) {
        val context = getApplication<Application>()
        context.startService(Intent(context, TimerService::class.java).setAction(action))
    }

    companion object {
        private const val PREFS_NAME = "clock_app_prefs"
        private const val KEY_PRESETS_SEEDED = "timer_presets_seeded"
    }
}