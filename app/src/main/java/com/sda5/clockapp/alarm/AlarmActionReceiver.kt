package com.sda5.clockapp.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.sda5.clockapp.ClockApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, -1L)
        if (alarmId == -1L) return

        // Stopping the service tears down the media player, vibration, and
        // the notification together — no separate cancel() call needed.
        context.stopService(Intent(context, AlarmRingingService::class.java))

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as ClockApplication
                val alarm = app.database.alarmDao().getById(alarmId) ?: return@launch

                if (intent.action == ACTION_SNOOZE) {
                    app.alarmScheduler.scheduleSnooze(alarm)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_DISMISS = "com.sda5.clockapp.ACTION_DISMISS"
        const val ACTION_SNOOZE = "com.sda5.clockapp.ACTION_SNOOZE"
    }
}