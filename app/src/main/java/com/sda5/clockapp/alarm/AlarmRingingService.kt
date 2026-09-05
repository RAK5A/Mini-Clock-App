package com.sda5.clockapp.alarm

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.getSystemService
import com.sda5.clockapp.model.Alarm

class AlarmRingingService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService<VibratorManager>()?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService<Vibrator>()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val alarm = Alarm(
            id = intent.getLongExtra(EXTRA_ALARM_ID, -1L),
            hour = intent.getIntExtra(EXTRA_HOUR, 0),
            minute = intent.getIntExtra(EXTRA_MINUTE, 0),
            label = intent.getStringExtra(EXTRA_LABEL) ?: "",
            soundEnabled = intent.getBooleanExtra(EXTRA_SOUND_ENABLED, true),
            soundUri = intent.getStringExtra(EXTRA_SOUND_URI),
            vibrationEnabled = intent.getBooleanExtra(EXTRA_VIBRATION_ENABLED, true),
            snoozeEnabled = intent.getBooleanExtra(EXTRA_SNOOZE_ENABLED, true),
            snoozeIntervalMinutes = intent.getIntExtra(EXTRA_SNOOZE_INTERVAL_MINUTES, 5),
            snoozeRepeatLimit = intent.getIntExtra(EXTRA_SNOOZE_REPEAT_LIMIT, 3).takeIf { it != NO_LIMIT }
        )
        val snoozeCount = intent.getIntExtra(EXTRA_SNOOZE_COUNT, 0)

        startForeground(
            alarm.id.hashCode(),
            NotificationHelper.buildRingingNotification(this, alarm, snoozeCount)
        )

        if (alarm.soundEnabled) startRinging(alarm.soundUri)
        if (alarm.vibrationEnabled) startVibrating()

        return START_STICKY
    }

    private fun startRinging(soundUri: String?) {
        val uri = soundUri?.let(Uri::parse)
            ?: RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getValidRingtoneUri(this)

        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(this@AlarmRingingService, uri)
            isLooping = true
            prepare()
            start()
        }
    }

    private fun startVibrating() {
        val pattern = longArrayOf(0, 800, 800)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    override fun onDestroy() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
        vibrator?.cancel()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_ALARM_ID = "extra_alarm_id"
        const val EXTRA_LABEL = "extra_label"
        const val EXTRA_HOUR = "extra_hour"
        const val EXTRA_MINUTE = "extra_minute"
        const val EXTRA_SOUND_ENABLED = "extra_sound_enabled"
        const val EXTRA_SOUND_URI = "extra_sound_uri"
        const val EXTRA_VIBRATION_ENABLED = "extra_vibration_enabled"
        const val EXTRA_SNOOZE_ENABLED = "extra_snooze_enabled"
        const val EXTRA_SNOOZE_INTERVAL_MINUTES = "extra_snooze_interval_minutes"
        const val EXTRA_SNOOZE_REPEAT_LIMIT = "extra_snooze_repeat_limit"
        const val EXTRA_SNOOZE_COUNT = "extra_snooze_count"
        const val NO_LIMIT = -1
    }
}