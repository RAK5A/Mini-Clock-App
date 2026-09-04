package com.sda5.clockapp.timer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sda5.clockapp.ui.components.LiveNotificationCard
import com.sda5.clockapp.ui.components.TimeWheelPicker
import com.sda5.clockapp.ui.theme.ClockBackground
import com.sda5.clockapp.ui.theme.ClockChipBg
import com.sda5.clockapp.ui.theme.ClockChipBorder
import com.sda5.clockapp.ui.theme.ClockDisabledStartBg
import com.sda5.clockapp.ui.theme.ClockDisabledStartText
import com.sda5.clockapp.ui.theme.ClockMutedText
import com.sda5.clockapp.ui.theme.ClockPauseRed
import com.sda5.clockapp.ui.theme.ClockPrimary
import com.sda5.clockapp.ui.theme.ClockPrimaryVariant
import com.sda5.clockapp.ui.theme.ClockSurfaceElevated
import com.sda5.clockapp.ui.theme.GlassBorder
import com.sda5.clockapp.ui.theme.GlassSurface
import com.sda5.clockapp.ui.theme.TimerGradientBrush
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    modifier: Modifier = Modifier,
    viewModel: TimerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ClockBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Timer",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ClockBackground
                ),
                actions = {
                    if (uiState.status == TimerStatus.RUNNING || uiState.status == TimerStatus.PAUSED) {
                        IconButton(onClick = { viewModel.toggleLiveNotification() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                                contentDescription = "Toggle Live Banner",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = {
                            viewModel.addPreset(uiState.hours, uiState.minutes, uiState.seconds)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Preset",
                                tint = Color.White
                            )
                        }
                    }
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
            // Live Notification Card Banner
            AnimatedVisibility(
                visible = (uiState.status == TimerStatus.RUNNING || uiState.status == TimerStatus.PAUSED) && uiState.showLiveNotification,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LiveNotificationCard(
                    remainingDisplay = uiState.liveNotificationDisplay,
                    totalDisplay = uiState.totalDurationDisplay,
                    targetFinishTime = uiState.targetFinishTime,
                    isRunning = uiState.status == TimerStatus.RUNNING,
                    onPauseResumeToggle = {
                        if (uiState.status == TimerStatus.RUNNING) viewModel.pauseTimer()
                        else viewModel.resumeTimer()
                    },
                    onClose = { viewModel.toggleLiveNotification() }
                )
            }

            Spacer(modifier = Modifier.weight(0.1f))

            when (uiState.status) {
                TimerStatus.SETUP -> {
                    // SETUP VIEW
                    TimeWheelPicker(
                        hours = uiState.hours,
                        minutes = uiState.minutes,
                        seconds = uiState.seconds,
                        onHoursChange = { viewModel.setHours(it) },
                        onMinutesChange = { viewModel.setMinutes(it) },
                        onSecondsChange = { viewModel.setSeconds(it) },
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Presets row
                    PresetsRow(
                        presets = uiState.presets,
                        onSelectPreset = { viewModel.applyPreset(it) },
                        onDeletePreset = { viewModel.deletePreset(it) },
                        onAddCurrentAsPreset = {
                            viewModel.addPreset(uiState.hours, uiState.minutes, uiState.seconds)
                        },
                        isCurrentValid = uiState.isStartEnabled
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Bottom Start Button
                    StartButton(
                        isEnabled = uiState.isStartEnabled,
                        onClick = { viewModel.startTimer() },
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }

                TimerStatus.RUNNING, TimerStatus.PAUSED, TimerStatus.FINISHED -> {
                    // COUNTDOWN VIEW
                    ActiveCountdownView(
                        uiState = uiState,
                        modifier = Modifier.weight(1f)
                    )

                    // Control Buttons (Delete & Pause/Resume/Restart)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Delete / Cancel Pill Button
                        Button(
                            onClick = { viewModel.deleteTimer() },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .padding(end = 8.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ClockSurfaceElevated,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Delete",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Pause / Resume / Reset Pill Button
                        val buttonText = when (uiState.status) {
                            TimerStatus.FINISHED -> "Reset"
                            TimerStatus.RUNNING -> "Pause"
                            TimerStatus.PAUSED -> "Resume"
                            else -> "Start"
                        }

                        val isGradientBtn = uiState.status != TimerStatus.RUNNING

                        Button(
                            onClick = {
                                when (uiState.status) {
                                    TimerStatus.RUNNING -> viewModel.pauseTimer()
                                    TimerStatus.PAUSED -> viewModel.resumeTimer()
                                    TimerStatus.FINISHED -> viewModel.deleteTimer()
                                    else -> {}
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .padding(start = 8.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .then(
                                    if (isGradientBtn) Modifier.background(TimerGradientBrush)
                                    else Modifier.background(ClockPauseRed)
                                ),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                text = buttonText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetsRow(
    presets: List<PresetTime>,
    onSelectPreset: (PresetTime) -> Unit,
    onDeletePreset: (PresetTime) -> Unit,
    onAddCurrentAsPreset: () -> Unit,
    isCurrentValid: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "PRESETS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = ClockMutedText,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(presets, key = { it.id }) { preset ->
                PresetChip(
                    preset = preset,
                    onClick = { onSelectPreset(preset) },
                    onLongClick = { onDeletePreset(preset) }
                )
                Spacer(modifier = Modifier.width(14.dp))
            }

            if (isCurrentValid) {
                item {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(GlassSurface)
                            .border(1.dp, GlassBorder, CircleShape)
                            .combinedClickable(onClick = onAddCurrentAsPreset),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Save Preset",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PresetChip(
    preset: PresetTime,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(ClockChipBg)
            .border(1.dp, ClockChipBorder, CircleShape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = preset.formattedString,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StartButton(
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier
            .width(200.dp)
            .height(58.dp)
            .clip(RoundedCornerShape(29.dp))
            .then(
                if (isEnabled) Modifier.background(TimerGradientBrush)
                else Modifier.background(ClockDisabledStartBg)
            ),
        shape = RoundedCornerShape(29.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = ClockDisabledStartText
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Start",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ActiveCountdownView(
    uiState: TimerUiState,
    modifier: Modifier = Modifier
) {
    val animatedFraction by animateFloatAsState(
        targetValue = uiState.remainingFraction,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "RingProgress"
    )

    // Pulsing effect for finished alarm state
    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // Animated Canvas with Ticks & Progress Arc
        Canvas(modifier = Modifier.size(320.dp)) {
            val strokeWidth = 14.dp.toPx()
            val diameter = size.minDimension - strokeWidth * 2
            val radius = diameter / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Draw 60 clock ticks
            val tickCount = 60
            for (i in 0 until tickCount) {
                val angleInRad = Math.toRadians((i * (360f / tickCount) - 90).toDouble())
                val isMajor = i % 5 == 0
                val tickLength = if (isMajor) 12.dp.toPx() else 6.dp.toPx()
                val tickWidth = if (isMajor) 2.5.dp.toPx() else 1.5.dp.toPx()

                val innerRadius = radius - strokeWidth / 2 - 8.dp.toPx()
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

            // Dark background ring
            drawCircle(
                color = ClockSurfaceElevated,
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            // Dynamic progress arc with Brush Gradient
            val sweepAngle = 360f * (if (uiState.status == TimerStatus.FINISHED) 1f else animatedFraction)

            if (uiState.status == TimerStatus.FINISHED) {
                drawArc(
                    color = ClockPrimary.copy(alpha = pulseAlpha),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            } else {
                drawArc(
                    brush = TimerGradientBrush,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Inside Circle Details
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Total Duration Header (e.g. "3 m")
            Text(
                text = uiState.totalDurationDisplay,
                color = ClockMutedText,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Large Time Remaining Display
            Text(
                text = if (uiState.status == TimerStatus.FINISHED) "00:00" else uiState.remainingDisplay,
                color = Color.White,
                fontSize = 62.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Target Finish Time with Bell Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(GlassSurface)
                    .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = if (uiState.status == TimerStatus.FINISHED) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                    contentDescription = "Target Finish Time",
                    tint = ClockPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (uiState.status == TimerStatus.FINISHED) "Time's up!" else uiState.targetFinishTime,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}