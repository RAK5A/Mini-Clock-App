package com.sda5.clockapp.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.sda5.clockapp.ClockApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        if (alarmId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as ClockApplication
                val alarm = app.database.alarmDao().getById(alarmId) ?: return@launch

                val ringIntent = Intent(context, AlarmRingingService::class.java).apply {
                    putExtra(AlarmRingingService.EXTRA_ALARM_ID, alarm.id)
                    putExtra(AlarmRingingService.EXTRA_LABEL, alarm.label)
                    putExtra(AlarmRingingService.EXTRA_HOUR, alarm.hour)
                    putExtra(AlarmRingingService.EXTRA_MINUTE, alarm.minute)
                    putExtra(AlarmRingingService.EXTRA_SOUND_ENABLED, alarm.soundEnabled)
                    putExtra(AlarmRingingService.EXTRA_SOUND_URI, alarm.soundUri)
                    putExtra(AlarmRingingService.EXTRA_VIBRATION_ENABLED, alarm.vibrationEnabled)
                    putExtra(AlarmRingingService.EXTRA_SNOOZE_ENABLED, alarm.snoozeEnabled)
                }
                ContextCompat.startForegroundService(context, ringIntent)

                if (alarm.repeatDays.isNotEmpty()) {
                    app.alarmScheduler.schedule(alarm)
                } else {
                    app.database.alarmDao().upsert(alarm.copy(isEnabled = false))
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"
    }
}