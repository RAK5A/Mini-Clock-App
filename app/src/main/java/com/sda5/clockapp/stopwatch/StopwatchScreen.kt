package com.sda5.clockapp.stopwatch

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sda5.clockapp.ui.theme.ClockBackground
import com.sda5.clockapp.ui.theme.ClockMutedText
import com.sda5.clockapp.ui.theme.ClockSurfaceElevated
import com.sda5.clockapp.ui.theme.GlassBorder
import com.sda5.clockapp.ui.theme.GlassSurface
import com.sda5.clockapp.ui.theme.StopwatchAccent
import com.sda5.clockapp.ui.theme.StopwatchGradientBrush
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

data class LapItem(
    val lapIndex: Int,
    val lapTimeMillis: Long,
    val totalTimeMillis: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopwatchScreen(modifier: Modifier = Modifier) {
    var isRunning by remember { mutableStateOf(false) }
    var elapsedTimeMillis by remember { mutableLongStateOf(0L) }
    var lastLapTimeMillis by remember { mutableLongStateOf(0L) }
    val laps = remember { mutableStateListOf<LapItem>() }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            val startTime = System.currentTimeMillis() - elapsedTimeMillis
            while (isRunning) {
                elapsedTimeMillis = System.currentTimeMillis() - startTime
                delay(10)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ClockBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Stopwatch",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ClockBackground
                ),
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Circular Stopwatch Counter Ring
            StopwatchDisplayRing(elapsedTimeMillis = elapsedTimeMillis)

            Spacer(modifier = Modifier.height(24.dp))

            // Controls (Lap/Reset & Start/Stop)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Lap / Reset Button
                Button(
                    onClick = {
                        if (isRunning) {
                            val currentLapDiff = elapsedTimeMillis - lastLapTimeMillis
                            laps.add(
                                0,
                                LapItem(
                                    lapIndex = laps.size + 1,
                                    lapTimeMillis = currentLapDiff,
                                    totalTimeMillis = elapsedTimeMillis
                                )
                            )
                            lastLapTimeMillis = elapsedTimeMillis
                        } else {
                            // Reset
                            elapsedTimeMillis = 0L
                            lastLapTimeMillis = 0L
                            laps.clear()
                        }
                    },
                    enabled = isRunning || elapsedTimeMillis > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(end = 8.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ClockSurfaceElevated,
                        contentColor = Color.White,
                        disabledContainerColor = GlassSurface,
                        disabledContentColor = ClockMutedText.copy(alpha = 0.4f)
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Flag else Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRunning) "Lap" else "Reset",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Start / Stop Button
                Button(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(start = 8.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .then(
                            if (isRunning) Modifier.background(Color(0xFFFF3B30))
                            else Modifier.background(StopwatchGradientBrush)
                        ),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isRunning) "Stop" else "Start",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Lap List Header
            if (laps.isNotEmpty()) {
                Text(
                    text = "LAPS RECORD",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ClockMutedText,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 24.dp, bottom = 8.dp)
                )
            }

            // Lap List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val fastestLap = laps.minByOrNull { it.lapTimeMillis }
                val slowestLap = if (laps.size > 1) laps.maxByOrNull { it.lapTimeMillis } else null

                itemsIndexed(laps, key = { _, item -> item.lapIndex }) { _, item ->
                    val isFastest = laps.size > 1 && item == fastestLap
                    val isSlowest = laps.size > 1 && item == slowestLap

                    LapRowCard(
                        lap = item,
                        isFastest = isFastest,
                        isSlowest = isSlowest
                    )
                }
            }
        }
    }
}

@Composable
private fun StopwatchDisplayRing(elapsedTimeMillis: Long) {
    val seconds = (elapsedTimeMillis / 1000) % 60
    val minutes = (elapsedTimeMillis / 60000) % 60
    val millisFraction = (elapsedTimeMillis % 1000) / 10

    val sweepAngle = ((elapsedTimeMillis % 60000) / 60000f) * 360f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(270.dp)) {
            val strokeWidth = 12.dp.toPx()
            val diameter = size.minDimension - strokeWidth * 2
            val radius = diameter / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // 60 Ticks
            for (i in 0 until 60) {
                val angleInRad = Math.toRadians((i * 6f - 90).toDouble())
                val isMajor = i % 5 == 0
                val tickLength = if (isMajor) 10.dp.toPx() else 5.dp.toPx()
                val tickWidth = if (isMajor) 2.dp.toPx() else 1.2.dp.toPx()

                val innerRadius = radius - strokeWidth / 2 - 6.dp.toPx()
                val outerRadius = innerRadius - tickLength

                val start = Offset(
                    (center.x + innerRadius * cos(angleInRad)).toFloat(),
                    (center.y + innerRadius * sin(angleInRad)).toFloat()
                )
                val end = Offset(
                    (center.x + outerRadius * cos(angleInRad)).toFloat(),
                    (center.y + outerRadius * sin(angleInRad)).toFloat()
                )

                drawLine(
                    color = if (isMajor) ClockMutedText.copy(alpha = 0.4f) else ClockMutedText.copy(alpha = 0.15f),
                    start = start,
                    end = end,
                    strokeWidth = tickWidth,
                    cap = StrokeCap.Round
                )
            }

            // Dark ring
            drawCircle(
                color = ClockSurfaceElevated,
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            // Sweep Arc
            drawArc(
                brush = StopwatchGradientBrush,
                startAngle = -90f,
                sweepAngle = if (sweepAngle == 0f) 0.1f else sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                    fontSize = 54.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = String.format(Locale.getDefault(), ".%02d", millisFraction),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = StopwatchAccent,
                    modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun LapRowCard(
    lap: LapItem,
    isFastest: Boolean,
    isSlowest: Boolean
) {
    val textColor = when {
        isFastest -> Color(0xFF4ADE80)
        isSlowest -> Color(0xFFF87171)
        else -> Color.White
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ClockSurfaceElevated)
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Lap ${lap.lapIndex}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            if (isFastest) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BEST",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4ADE80),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF4ADE80).copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            } else if (isSlowest) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SLOWEST",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF87171),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF87171).copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        val lapSec = (lap.lapTimeMillis / 1000) % 60
        val lapMin = (lap.lapTimeMillis / 60000) % 60
        val lapMillis = (lap.lapTimeMillis % 1000) / 10

        Text(
            text = String.format(Locale.getDefault(), "%02d:%02d.%02d", lapMin, lapSec, lapMillis),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}