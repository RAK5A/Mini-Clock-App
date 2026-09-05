package com.sda5.clockapp.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import com.sda5.clockapp.MainActivity
import com.sda5.clockapp.model.Alarm
import java.time.LocalDateTime
import java.time.ZoneId

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService<AlarmManager>()

    fun schedule(alarm: Alarm) {
        val trigger = nextTrigger(alarm) ?: return
        scheduleAt(alarm, trigger, snoozeCount = 0)
    }

    fun scheduleSnooze(alarm: Alarm, snoozeCount: Int) {
        val trigger = LocalDateTime.now().plusMinutes(alarm.snoozeIntervalMinutes.toLong())
        scheduleAt(alarm, trigger, snoozeCount)
    }

    private fun scheduleAt(alarm: Alarm, triggerTime: LocalDateTime, snoozeCount: Int) {
        val manager = alarmManager ?: return
        val triggerMillis = triggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val requestCode = alarm.id.hashCode()

        val showIntent = PendingIntent.getActivity(
            context,
            requestCode,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val operation = PendingIntent.getBroadcast(
            context,
            requestCode,
            Intent(context, AlarmReceiver::class.java)
                .putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
                .putExtra(AlarmReceiver.EXTRA_SNOOZE_COUNT, snoozeCount),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        manager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerMillis, showIntent), operation)
    }

    fun cancel(alarm: Alarm) {
        val manager = alarmManager ?: return
        val operation = PendingIntent.getBroadcast(
            context,
            alarm.id.hashCode(),
            Intent(context, AlarmReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        manager.cancel(operation)
    }
}