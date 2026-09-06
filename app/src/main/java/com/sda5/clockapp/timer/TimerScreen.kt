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
// import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sda5.clockapp.ui.components.StartButton
// import com.sda5.clockapp.ui.components.LiveNotificationCard
import com.sda5.clockapp.ui.components.TimeWheelPicker
import com.sda5.clockapp.ui.theme.ClockAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    modifier: Modifier = Modifier,
    viewModel: TimerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    if (uiState.status == TimerStatus.RUNNING || uiState.status == TimerStatus.PAUSED) {
                        // Live Notification toggle — commented out along with the banner itself below
                        // IconButton(onClick = { viewModel.toggleLiveNotification() }) {
                        //     Icon(
                        //         imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                        //         contentDescription = "Toggle Live Banner",
                        //         tint = MaterialTheme.colorScheme.onSurface
                        //     )
                        // }
                        IconButton(onClick = {
                            viewModel.addPreset(uiState.hours, uiState.minutes, uiState.seconds)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Preset",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = MaterialTheme.colorScheme.onSurface
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
            // Live Notification banner — commented out for now (its component, LiveNotificationCard,
            // hasn't been added to this project yet)
            // AnimatedVisibility(
            //     visible = (uiState.status == TimerStatus.RUNNING || uiState.status == TimerStatus.PAUSED) && uiState.showLiveNotification,
            //     enter = fadeIn(),
            //     exit = fadeOut()
            // ) {
            //     LiveNotificationCard(
            //         remainingDisplay = uiState.liveNotificationDisplay,
            //         totalDisplay = uiState.totalDurationDisplay,
            //         targetFinishTime = uiState.targetFinishTime,
            //         isRunning = uiState.status == TimerStatus.RUNNING,
            //         onPauseResumeToggle = {
            //             if (uiState.status == TimerStatus.RUNNING) viewModel.pauseTimer()
            //             else viewModel.resumeTimer()
            //         },
            //         onClose = { viewModel.toggleLiveNotification() }
            //     )
            // }

            Spacer(modifier = Modifier.weight(0.1f))

            when (uiState.status) {
                TimerStatus.SETUP -> {
                    TimeWheelPicker(
                        hours = uiState.hours,
                        minutes = uiState.minutes,
                        seconds = uiState.seconds,
                        onHoursChange = { viewModel.setHours(it) },
                        onMinutesChange = { viewModel.setMinutes(it) },
                        onSecondsChange = { viewModel.setSeconds(it) },
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

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

                    StartButton(
                        text = "Start",
                        isEnabled = uiState.isStartEnabled,
                        onClick = { viewModel.startTimer() },
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }

                TimerStatus.RUNNING, TimerStatus.PAUSED, TimerStatus.FINISHED -> {
                    ActiveCountdownView(
                        uiState = uiState,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { viewModel.deleteTimer() },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .padding(end = 8.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = "Delete",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        val buttonBgColor = when (uiState.status) {
                            TimerStatus.FINISHED -> MaterialTheme.colorScheme.primary
                            TimerStatus.RUNNING -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.primary
                        }
                        val buttonText = when (uiState.status) {
                            TimerStatus.FINISHED -> "Reset"
                            TimerStatus.RUNNING -> "Pause"
                            TimerStatus.PAUSED -> "Resume"
                            else -> "Start"
                        }

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
                                .padding(start = 8.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonBgColor,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = buttonText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
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
            Spacer(modifier = Modifier.width(16.dp))
        }

        if (isCurrentValid) {
            item {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .combinedClickable(onClick = onAddCurrentAsPreset),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Save Preset",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
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
            .size(70.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = preset.formattedString,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
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

    val infiniteTransition = rememberInfiniteTransition(label = "PulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val ringTrackColor = MaterialTheme.colorScheme.surfaceVariant
    val ringActiveColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(310.dp)) {
            val strokeWidth = 12.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val radius = diameter / 2f

            drawCircle(
                color = ringTrackColor,
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            val sweepAngle = 360f * (if (uiState.status == TimerStatus.FINISHED) 1f else animatedFraction)
            val ringColor = if (uiState.status == TimerStatus.FINISHED) {
                ringActiveColor.copy(alpha = pulseAlpha)
            } else {
                ringActiveColor
            }

            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = uiState.totalDurationDisplay,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = if (uiState.status == TimerStatus.FINISHED) "00:00" else uiState.remainingDisplay,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (uiState.status == TimerStatus.FINISHED) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                    contentDescription = "Target Finish Time",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (uiState.status == TimerStatus.FINISHED) "Time's up!" else uiState.targetFinishTime,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun TimerScreenPreview() {
    val previewViewModel = remember { TimerViewModel() }

    ClockAppTheme {
        TimerScreen(viewModel = previewViewModel)
    }
}