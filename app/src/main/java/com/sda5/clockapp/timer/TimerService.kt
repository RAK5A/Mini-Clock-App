package com.sda5.clockapp.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimerService : Service(), LifecycleEventObserver {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tickJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isAppInForeground = false

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService<VibratorManager>()?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService<Vibrator>()
        }
    }

    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val hasActiveTimer = TimerState.uiState.value.status != TimerStatus.SETUP
        when (event) {
            Lifecycle.Event.ON_START -> {
                isAppInForeground = true
                if (hasActiveTimer) stopForeground(STOP_FOREGROUND_REMOVE)
            }
            Lifecycle.Event.ON_STOP -> {
                isAppInForeground = false
                if (hasActiveTimer) startForeground(NOTIFICATION_ID, buildCountdownNotification())
            }
            else -> {}
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startCountdown(intent.getLongExtra(EXTRA_TOTAL_SECONDS, 0L))
            ACTION_PAUSE -> pauseCountdown()
            ACTION_RESUME -> resumeCountdown()
            ACTION_CANCEL, ACTION_DISMISS -> stopSelf()
        }
        return START_STICKY
    }

    private fun startCountdown(totalSeconds: Long) {
        if (totalSeconds <= 0L) {
            stopSelf()
            return
        }
        createChannel()
        TimerState.update {
            it.copy(
                status = TimerStatus.RUNNING,
                totalSeconds = totalSeconds,
                remainingMillis = totalSeconds * 1000L,
                targetFinishTime = formatFinishTime(System.currentTimeMillis() + totalSeconds * 1000L)
            )
        }
        // Android requires this call the moment a foreground service starts — no way around it.
        // If the app is visible right now, we demote immediately after.
        startForeground(NOTIFICATION_ID, buildCountdownNotification())
        if (isAppInForeground) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
        runTick()
    }

    private fun pauseCountdown() {
        tickJob?.cancel()
        TimerState.update { it.copy(status = TimerStatus.PAUSED) }
        updateNotification()
    }

    private fun resumeCountdown() {
        val remaining = TimerState.uiState.value.remainingMillis
        TimerState.update {
            it.copy(status = TimerStatus.RUNNING, targetFinishTime = formatFinishTime(System.currentTimeMillis() + remaining))
        }
        runTick()
    }

    private fun runTick() {
        tickJob?.cancel()
        tickJob = serviceScope.launch {
            val startTime = System.currentTimeMillis()
            val initialMillis = TimerState.uiState.value.remainingMillis
            var lastNotifiedSecond = -1L

            while (TimerState.uiState.value.status == TimerStatus.RUNNING) {
                val elapsed = System.currentTimeMillis() - startTime
                val remaining = (initialMillis - elapsed).coerceAtLeast(0L)
                TimerState.update { it.copy(remainingMillis = remaining) }

                val currentSecond = remaining / 1000L
                if (currentSecond != lastNotifiedSecond) {
                    updateNotification()
                    lastNotifiedSecond = currentSecond
                }

                if (remaining <= 0L) {
                    onFinished()
                    break
                }
                delay(200L)
            }
        }
    }

    private fun onFinished() {
        TimerState.update { it.copy(status = TimerStatus.FINISHED, remainingMillis = 0L) }
        startRinging()
        updateNotification()
    }

    private fun startRinging() {
        val uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getValidRingtoneUri(this)
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(this@TimerService, uri)
            isLooping = true
            prepare()
            start()
        }
        val pattern = longArrayOf(0, 800, 800)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopRinging() {
        mediaPlayer?.apply { stop(); release() }
        mediaPlayer = null
        vibrator?.cancel()
    }

    private fun formatFinishTime(millis: Long): String =
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))

    private fun createChannel() {
        val manager = getSystemService<NotificationManager>() ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        val channel = NotificationChannel(CHANNEL_ID, "Timer", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Timer countdown and completion alerts"
            setSound(null, null)
            enableVibration(false)
        }
        manager.createNotificationChannel(channel)
    }

    private fun updateNotification() {
        // Skip entirely while the app is visible — the running screen already
        // shows this info, and the user asked not to see it duplicated as a system notification.
        if (isAppInForeground) return
        getSystemService<NotificationManager>()?.notify(NOTIFICATION_ID, buildCountdownNotification())
    }

    private fun buildCountdownNotification(): Notification {
        val state = TimerState.uiState.value
        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(state.status != TimerStatus.FINISHED)
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)

        when (state.status) {
            TimerStatus.FINISHED -> builder
                .setContentTitle("Time's up!")
                .setContentText("Your timer has finished")
                .addAction(0, "Dismiss", actionPendingIntent(ACTION_DISMISS))
            TimerStatus.PAUSED -> builder
                .setContentTitle("Timer paused")
                .setContentText(remainingText(state.remainingMillis))
                .addAction(0, "Resume", actionPendingIntent(ACTION_RESUME))
                .addAction(0, "Cancel", actionPendingIntent(ACTION_CANCEL))
            else -> builder
                .setContentTitle("Timer running")
                .setContentText(remainingText(state.remainingMillis))
                .addAction(0, "Pause", actionPendingIntent(ACTION_PAUSE))
                .addAction(0, "Cancel", actionPendingIntent(ACTION_CANCEL))
        }
        return builder.build()
    }

    private fun remainingText(remainingMillis: Long): String {
        val remSec = (remainingMillis + 999L) / 1000L
        val h = remSec / 3600
        val m = (remSec % 3600) / 60
        val s = remSec % 60
        return if (h > 0) String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s)
        else String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }

    private fun actionPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, TimerService::class.java).setAction(action)
        return PendingIntent.getService(
            this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        tickJob?.cancel()
        serviceScope.cancel()
        stopRinging()
        TimerState.update {
            it.copy(status = TimerStatus.SETUP, totalSeconds = 0L, remainingMillis = 0L, targetFinishTime = "")
        }
        super.onDestroy()
    }

    companion object {
        const val ACTION_START = "com.sda5.clockapp.timer.ACTION_START"
        const val ACTION_PAUSE = "com.sda5.clockapp.timer.ACTION_PAUSE"
        const val ACTION_RESUME = "com.sda5.clockapp.timer.ACTION_RESUME"
        const val ACTION_CANCEL = "com.sda5.clockapp.timer.ACTION_CANCEL"
        const val ACTION_DISMISS = "com.sda5.clockapp.timer.ACTION_DISMISS"
        const val EXTRA_TOTAL_SECONDS = "extra_total_seconds"
        const val CHANNEL_ID = "timer_channel"
        const val NOTIFICATION_ID = 5001
    }
}