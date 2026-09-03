package com.sda5.clockapp.alarm

import androidx.lifecycle.ViewModel
import com.sda5.clockapp.model.Alarm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime

class AlarmViewModel : ViewModel() {

    private val _alarms = MutableStateFlow<List<Alarm>>(emptyList())
    val alarms: StateFlow<List<Alarm>> = _alarms.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun addAlarm(alarm: Alarm) {
        _alarms.value = _alarms.value + alarm
        announce(alarm)
    }

    fun updateAlarm(alarm: Alarm) {
        _alarms.value = _alarms.value.map { if (it.id == alarm.id) alarm else it }
        announce(alarm)
    }

    fun deleteAlarm(id: Long) {
        _alarms.value = _alarms.value.filterNot { it.id == id }
    }

    fun deleteAllAlarms() {
        _alarms.value = emptyList()
    }

    fun setEnabled(id: Long, enabled: Boolean) {
        _alarms.value = _alarms.value.map {
            if (it.id == id) it.copy(isEnabled = enabled) else it
        }
    }

    fun consumeSnackbarMessage() {
        _snackbarMessage.value = null
    }

    private fun announce(alarm: Alarm) {
        if (!alarm.isEnabled) return
        val next = nextTrigger(alarm) ?: return
        val (hours, minutes) = countdownText(LocalDateTime.now(), next)
        _snackbarMessage.value = "Alarm set for $hours hours and $minutes minutes from now."
    }
}