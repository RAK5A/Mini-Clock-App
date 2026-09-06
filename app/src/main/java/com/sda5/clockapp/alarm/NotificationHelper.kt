package com.sda5.clockapp.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.sda5.clockapp.model.Alarm
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object NotificationHelper {
    const val CHANNEL_ID = "alarm_channel_v2"

    fun createChannel(context: Context) {
        val manager = context.getSystemService<NotificationManager>() ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarm ringing notifications"
            setSound(null, null)
            enableVibration(false)
        }
        manager.createNotificationChannel(channel)
    }

    fun buildRingingNotification(context: Context, alarm: Alarm, snoozeCount: Int): Notification {
        val requestCode = alarm.id.hashCode()

        val dismissIntent = Intent(context, AlarmActionReceiver::class.java).apply {
            action = AlarmActionReceiver.ACTION_DISMISS
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context, requestCode, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeText = LocalTime.of(alarm.hour, alarm.minute)
            .format(DateTimeFormatter.ofPattern("h:mm a"))

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(alarm.label.ifBlank { "Alarm" })
            .setContentText(timeText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .addAction(0, "Dismiss", dismissPendingIntent)

        val canSnooze = alarm.snoozeEnabled &&
                (alarm.snoozeRepeatLimit == null || snoozeCount < alarm.snoozeRepeatLimit)

        if (canSnooze) {
            val snoozeIntent = Intent(context, AlarmActionReceiver::class.java).apply {
                action = AlarmActionReceiver.ACTION_SNOOZE
                putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarm.id)
                putExtra(AlarmReceiver.EXTRA_SNOOZE_COUNT, snoozeCount)
            }
            val snoozePendingIntent = PendingIntent.getBroadcast(
                context, requestCode + 1, snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "Snooze", snoozePendingIntent)
        }

        return builder.build()
    }
}