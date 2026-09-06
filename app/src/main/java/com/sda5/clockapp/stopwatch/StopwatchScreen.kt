package com.sda5.clockapp.stopwatch

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

val DarkNavyBackground = Color(0xFF000000)
val BlueButtonColor = Color(0xFF2196F3)
val RedStopButtonColor = Color(0xFFE53935)
val SoftPinkTimeColor = Color(0xFFFFCDD2)

data class LapTableEntry(
    val lapNumber: Int,
    val lapDurationMs: Long,
    val overallTimeMs: Long
)

@Composable
fun StopwatchScreen(modifier: Modifier = Modifier) {
    var started by remember { mutableStateOf(false) }
    var elapsedTimeMs by remember { mutableLongStateOf(0L) }
    val laps = remember { mutableStateListOf<LapTableEntry>() }

    // High precision timer logic (hundredths of a second)
    LaunchedEffect(started) {
        if (started) {
            var lastTime = SystemClock.elapsedRealtime()
            while (started) {
                delay(10)
                val now = SystemClock.elapsedRealtime()
                elapsedTimeMs += (now - lastTime)
                lastTime = now
            }
        }
    }

    val timeDisplay = formatLapTime(elapsedTimeMs)

    Surface(
        modifier = modifier.fillMaxSize(),
        color = DarkNavyBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Time Display (No Circle Background)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timeDisplay,
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Lap Table History Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 16.dp)
            ) {
                // Table Header (Lap | Lap times | Overall time)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lap",
                        fontSize = 16.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start
                    )
                    Text(
                        text = "Lap times",
                        fontSize = 16.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1.5f),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Overall time",
                        fontSize = 16.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1.5f),
                        textAlign = TextAlign.End
                    )
                }

                HorizontalDivider(
                    color = Color.Gray.copy(alpha = 0.5f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                // Table List Items
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(laps) { lap ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Lap Number (e.g. 01, 02)
                            Text(
                                text = String.format(Locale.US, "%02d", lap.lapNumber),
                                fontSize = 18.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Normal,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start
                            )

                            // Lap Time (e.g. 00:01.74)
                            Text(
                                text = formatLapTime(lap.lapDurationMs),
                                fontSize = 18.sp,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1.5f),
                                textAlign = TextAlign.Center
                            )

                            // Overall Time (e.g. 00:01.74)
                            Text(
                                text = formatLapTime(lap.overallTimeMs),
                                fontSize = 18.sp,
                                color = SoftPinkTimeColor,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1.5f),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }

            // Controls Row (Start/Stop, Lap, Restart)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Solid Start / Stop Button
                Button(
                    onClick = { started = !started },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (started) RedStopButtonColor else BlueButtonColor,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (!started) "Start" else "Stop",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Solid Lap Button
                Button(
                    onClick = {
                        if (started) {
                            val previousOverall = laps.firstOrNull()?.overallTimeMs ?: 0L
                            val lapDuration = elapsedTimeMs - previousOverall
                            val nextNumber = laps.size + 1
                            laps.add(0, LapTableEntry(nextNumber, lapDuration, elapsedTimeMs))
                        }
                    },
                    enabled = started,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueButtonColor,
                        contentColor = Color.White,
                        disabledContainerColor = BlueButtonColor.copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = "Lap",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Solid Restart / Reset Button
                Button(
                    onClick = {
                        started = false
                        elapsedTimeMs = 0L
                        laps.clear()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = BlueButtonColor)
                ) {
                    Text(
                        text = "Restart",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun formatLapTime(milliseconds: Long): String {
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
