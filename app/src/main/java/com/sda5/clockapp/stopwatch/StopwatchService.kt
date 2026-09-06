package com.sda5.clockapp.stopwatch

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.sda5.clockapp.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class StopwatchService : Service(), LifecycleEventObserver {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tickJob: Job? = null
    private var isAppInForeground = false

    private var baseElapsedTimeMs: Long = 0L
    private var startTimestamp: Long = 0L

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val hasActiveStopwatch = StopwatchState.uiState.value.status != StopwatchStatus.IDLE
        when (event) {
            Lifecycle.Event.ON_START -> {
                isAppInForeground = true
                if (hasActiveStopwatch) stopForeground(STOP_FOREGROUND_REMOVE)
            }
            Lifecycle.Event.ON_STOP -> {
                isAppInForeground = false
                if (hasActiveStopwatch) startForeground(NOTIFICATION_ID, buildStopwatchNotification())
            }
            else -> {}
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startStopwatch()
            ACTION_PAUSE -> pauseStopwatch()
            ACTION_RESUME -> resumeStopwatch()
            ACTION_LAP -> recordLap()
            ACTION_RESET -> resetStopwatch()
        }
        return START_STICKY
    }

    private fun startStopwatch() {
        createChannel()
        baseElapsedTimeMs = StopwatchState.uiState.value.elapsedTimeMs
        startTimestamp = SystemClock.elapsedRealtime()

        StopwatchState.update { it.copy(status = StopwatchStatus.RUNNING) }

        startForeground(NOTIFICATION_ID, buildStopwatchNotification())
        if (isAppInForeground) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }

        runTick()
    }

    private fun pauseStopwatch() {
        tickJob?.cancel()
        if (startTimestamp > 0L) {
            baseElapsedTimeMs += (SystemClock.elapsedRealtime() - startTimestamp)
            startTimestamp = 0L
        }
        StopwatchState.update {
            it.copy(status = StopwatchStatus.PAUSED, elapsedTimeMs = baseElapsedTimeMs)
        }
        updateNotification()
    }

    private fun resumeStopwatch() {
        startTimestamp = SystemClock.elapsedRealtime()
        StopwatchState.update { it.copy(status = StopwatchStatus.RUNNING) }
        runTick()
        updateNotification()
    }

    private fun recordLap() {
        val currentState = StopwatchState.uiState.value
        val currentElapsed = if (startTimestamp > 0L) {
            baseElapsedTimeMs + (SystemClock.elapsedRealtime() - startTimestamp)
        } else {
            currentState.elapsedTimeMs
        }

        val previousOverall = currentState.laps.firstOrNull()?.overallTimeMs ?: 0L
        val lapDuration = currentElapsed - previousOverall
        val nextNumber = currentState.laps.size + 1
        val newLap = LapTableEntry(nextNumber, lapDuration, currentElapsed)

        StopwatchState.update {
            it.copy(
                elapsedTimeMs = currentElapsed,
                laps = listOf(newLap) + it.laps
            )
        }
    }

    private fun resetStopwatch() {
        tickJob?.cancel()
        baseElapsedTimeMs = 0L
        startTimestamp = 0L
        StopwatchState.update {
            StopwatchRunState(status = StopwatchStatus.IDLE, elapsedTimeMs = 0L, laps = emptyList())
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun runTick() {
        tickJob?.cancel()
        tickJob = serviceScope.launch {
            var lastNotifiedSecond = -1L
            while (StopwatchState.uiState.value.status == StopwatchStatus.RUNNING) {
                val currentElapsed = baseElapsedTimeMs + (SystemClock.elapsedRealtime() - startTimestamp)
                StopwatchState.update { it.copy(elapsedTimeMs = currentElapsed) }

                val currentSecond = currentElapsed / 1000L
                if (currentSecond != lastNotifiedSecond) {
                    updateNotification()
                    lastNotifiedSecond = currentSecond
                }
                delay(30L)
            }
        }
    }

    private fun createChannel() {
        val manager = getSystemService<NotificationManager>() ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Stopwatch",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Stopwatch active and elapsed time notifications"
            setSound(null, null)
            enableVibration(false)
        }
        manager.createNotificationChannel(channel)
    }

    private fun updateNotification() {
        if (isAppInForeground) return
        getSystemService<NotificationManager>()?.notify(NOTIFICATION_ID, buildStopwatchNotification())
    }

    private fun buildStopwatchNotification(): Notification {
        val state = StopwatchState.uiState.value

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("NAVIGATE_TO", "STOPWATCH")
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val contentIntent = PendingIntent.getActivity(
            this,
            1002,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(state.status == StopwatchStatus.RUNNING)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)

        when (state.status) {
            StopwatchStatus.PAUSED -> builder
                .setContentTitle("Stopwatch paused")
                .setContentText(formatNotificationTime(state.elapsedTimeMs))
                .addAction(0, "Resume", actionPendingIntent(ACTION_RESUME))
                .addAction(0, "Reset", actionPendingIntent(ACTION_RESET))
            else -> builder
                .setContentTitle("Stopwatch running")
                .setContentText(formatNotificationTime(state.elapsedTimeMs))
                .addAction(0, "Pause", actionPendingIntent(ACTION_PAUSE))
                .addAction(0, "Lap", actionPendingIntent(ACTION_LAP))
        }

        return builder.build()
    }

    private fun formatNotificationTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val seconds = totalSeconds % 60
        val totalMinutes = totalSeconds / 60
        val minutes = totalMinutes % 60
        val hours = totalMinutes / 60

        return if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    private fun actionPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, StopwatchService::class.java).setAction(action)
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        tickJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.sda5.clockapp.stopwatch.ACTION_START"
        const val ACTION_PAUSE = "com.sda5.clockapp.stopwatch.ACTION_PAUSE"
        const val ACTION_RESUME = "com.sda5.clockapp.stopwatch.ACTION_RESUME"
        const val ACTION_LAP = "com.sda5.clockapp.stopwatch.ACTION_LAP"
        const val ACTION_RESET = "com.sda5.clockapp.stopwatch.ACTION_RESET"

        const val CHANNEL_ID = "stopwatch_channel"
        const val NOTIFICATION_ID = 5002
    }
}
