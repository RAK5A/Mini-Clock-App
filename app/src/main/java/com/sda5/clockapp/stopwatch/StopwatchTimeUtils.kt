package com.sda5.clockapp.stopwatch

import java.util.Locale

fun formatLapTime(milliseconds: Long): String {
    val hundredths = (milliseconds % 1000) / 10
    val totalSeconds = milliseconds / 1000
    val seconds = totalSeconds % 60
    val totalMinutes = totalSeconds / 60
    val minutes = totalMinutes % 60
    val hours = totalMinutes / 60

    return if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d.%02d", hours, minutes, seconds, hundredths)
    } else {
        String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, hundredths)
    }
}