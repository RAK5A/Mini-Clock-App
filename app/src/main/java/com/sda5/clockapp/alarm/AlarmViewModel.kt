package com.sda5.clockapp.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sda5.clockapp.model.Alarm
import com.sda5.clockapp.data.alarms.AlarmDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class AlarmViewModel(
    private val dao: AlarmDao,
    private val scheduler: AlarmScheduler
) : ViewModel() {
    val alarms: StateFlow<List<Alarm>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun addAlarm(alarm: Alarm) = viewModelScope.launch {
        dao.upsert(alarm)
        scheduler.schedule(alarm)
        announce(alarm)
    }

    fun updateAlarm(alarm: Alarm) = viewModelScope.launch {
        dao.upsert(alarm)
        if (alarm.isEnabled) scheduler.schedule(alarm) else scheduler.cancel(alarm)
        announce(alarm)
    }

    fun deleteAlarm(id: Long) = viewModelScope.launch {
        val alarm = alarms.value.find { it.id == id } ?: return@launch
        scheduler.cancel(alarm)
        dao.delete(alarm)
    }

    fun deleteAllAlarms() = viewModelScope.launch {
        alarms.value.forEach { scheduler.cancel(it) }
        dao.deleteAll()
    }

    fun setEnabled(id: Long, enabled: Boolean) = viewModelScope.launch {
        val alarm = alarms.value.find { it.id == id } ?: return@launch
        val updated = alarm.copy(isEnabled = enabled)
        dao.upsert(updated)
        if (enabled) scheduler.schedule(updated) else scheduler.cancel(updated)
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