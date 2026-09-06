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

class StopwatchService : Service(), LifecycleEventObserver {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tickJob: Job? = null
    private var isAppInForeground = false

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val state = StopwatchState.uiState.value
        val hasActiveStopwatch = state.isRunning || state.elapsedMillis > 0L
        when (event) {
            Lifecycle.Event.ON_START -> {
                isAppInForeground = true
                if (hasActiveStopwatch) stopForeground(STOP_FOREGROUND_REMOVE)
            }
            Lifecycle.Event.ON_STOP -> {
                isAppInForeground = false
                if (hasActiveStopwatch) startForeground(NOTIFICATION_ID, buildNotification())
            }
            else -> {}
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startStopwatch()
            ACTION_PAUSE -> pauseStopwatch()
            ACTION_LAP -> addLap()
            ACTION_RESET -> stopSelf()
        }
        return START_STICKY
    }

    private fun startStopwatch() {
        createChannel()
        StopwatchState.update { it.copy(isRunning = true) }
        // Must call this the instant the service starts — Android requires it, no way around it.
        startForeground(NOTIFICATION_ID, buildNotification())
        if (isAppInForeground) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
        runTick()
    }

    private fun pauseStopwatch() {
        tickJob?.cancel()
        StopwatchState.update { it.copy(isRunning = false) }
        updateNotification()
    }

    private fun addLap() {
        val state = StopwatchState.uiState.value
        if (!state.isRunning) return
        val previousOverall = state.laps.firstOrNull()?.overallTimeMs ?: 0L
        val lapDuration = state.elapsedMillis - previousOverall
        val nextNumber = state.laps.size + 1
        StopwatchState.update {
            it.copy(laps = listOf(LapTableEntry(nextNumber, lapDuration, state.elapsedMillis)) + it.laps)
        }
        updateNotification()
    }

    private fun runTick() {
        tickJob?.cancel()
        tickJob = serviceScope.launch {
            var lastTime = SystemClock.elapsedRealtime()
            var lastNotifiedSecond = -1L
            while (StopwatchState.uiState.value.isRunning) {
                delay(10)
                val now = SystemClock.elapsedRealtime()
                val delta = now - lastTime
                lastTime = now
                StopwatchState.update { it.copy(elapsedMillis = it.elapsedMillis + delta) }

                val currentSecond = StopwatchState.uiState.value.elapsedMillis / 1000L
                if (currentSecond != lastNotifiedSecond) {
                    updateNotification()
                    lastNotifiedSecond = currentSecond
                }
            }
        }
    }

    private fun createChannel() {
        val manager = getSystemService<NotificationManager>() ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        // LOW importance — this is a status display, not something that should ever pop or ring.
        val channel = NotificationChannel(CHANNEL_ID, "Stopwatch", NotificationManager.IMPORTANCE_LOW).apply {
            description = "Ongoing stopwatch status"
            setSound(null, null)
            enableVibration(false)
        }
        manager.createNotificationChannel(channel)
    }

    private fun updateNotification() {
        if (isAppInForeground) return
        getSystemService<NotificationManager>()?.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val state = StopwatchState.uiState.value
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentTitle("Stopwatch")
            .setContentText(formatLapTime(state.elapsedMillis))

        if (state.isRunning) {
            builder.addAction(0, "Pause", actionPendingIntent(ACTION_PAUSE))
            builder.addAction(0, "Lap", actionPendingIntent(ACTION_LAP))
        } else {
            builder.addAction(0, "Resume", actionPendingIntent(ACTION_START))
            builder.addAction(0, "Reset", actionPendingIntent(ACTION_RESET))
        }
        return builder.build()
    }

    private fun actionPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, StopwatchService::class.java).setAction(action)
        return PendingIntent.getService(
            this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        tickJob?.cancel()
        serviceScope.cancel()
        StopwatchState.update { StopwatchRunState() }
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.sda5.clockapp.stopwatch.ACTION_START"
        const val ACTION_PAUSE = "com.sda5.clockapp.stopwatch.ACTION_PAUSE"
        const val ACTION_LAP = "com.sda5.clockapp.stopwatch.ACTION_LAP"
        const val ACTION_RESET = "com.sda5.clockapp.stopwatch.ACTION_RESET"
        const val CHANNEL_ID = "stopwatch_channel"
        const val NOTIFICATION_ID = 5002
    }
}