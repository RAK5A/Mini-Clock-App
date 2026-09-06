package com.sda5.clockapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.Locale

@Composable
fun TimeWheelPicker(
    hours: Int,
    minutes: Int,
    seconds: Int,
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        WheelHighlightBar()
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WheelColumn("Hours", hours, 0..99, onHoursChange, modifier = Modifier.weight(1f))
            WheelSeparator()
            WheelColumn("Minutes", minutes, 0..59, onMinutesChange, modifier = Modifier.weight(1f))
            WheelSeparator()
            WheelColumn("Seconds", seconds, 0..59, onSecondsChange, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun AlarmTimeWheelPicker(
    hour12: Int,
    minute: Int,
    isAm: Boolean,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onPeriodChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        WheelHighlightBar()
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WheelColumn("Hour", hour12, 1..12, onHourChange, modifier = Modifier.weight(1f))
            WheelSeparator()
            WheelColumn("Minute", minute, 0..59, onMinuteChange, modifier = Modifier.weight(1f))
            WheelColumn(
                label = "Period",
                value = if (isAm) 0 else 1,
                range = 0..1,
                onValueChange = { onPeriodChange(it == 0) },
                modifier = Modifier.weight(1f),
                loop = false,
                formatValue = { if (it == 0) "AM" else "PM" }
            )
        }
    }
}

@Composable
private fun WheelHighlightBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .height(64.dp)
            .padding(top = 18.dp)
//            .clip(RoundedCornerShape(18.dp))
//            .background(MaterialTheme.colorScheme.primaryContainer)
    )
}

@Composable
private fun WheelSeparator() {
    Text(
        text = ":",
        fontSize = 40.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 28.dp)
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelColumn(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 60.dp,
    loop: Boolean = true,
    formatValue: (Int) -> String = { String.format(Locale.getDefault(), "%02d", it) }
) {
    val items = remember(range) { range.toList() }
    val itemCount = items.size

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = if (loop) {
            (1000 * itemCount + (value - range.first) - 1).coerceAtLeast(0)
        } else {
            (value - range.first).coerceIn(0, itemCount - 1)
        }
    )
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val selectedIndex by remember {
        derivedStateOf {
            if (loop) {
                val firstVisibleIndex = listState.firstVisibleItemIndex
                val offset = listState.firstVisibleItemScrollOffset
                if (offset > itemHeight.value / 2) (firstVisibleIndex + 2) % itemCount
                else (firstVisibleIndex + 1) % itemCount
            } else {
                val layoutInfo = listState.layoutInfo
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                layoutInfo.visibleItemsInfo.minByOrNull { item ->
                    kotlin.math.abs((item.offset + item.size / 2) - viewportCenter)
                }?.index ?: 0
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { selectedIndex }
            .distinctUntilChanged()
            .collect { index -> onValueChange(range.first + index) }
    }

    LaunchedEffect(value) {
        val currentMappedVal = range.first + selectedIndex
        if (currentMappedVal != value) {
            if (loop) {
                val newTargetIndex = 1000 * itemCount + (value - range.first)
                listState.scrollToItem((newTargetIndex - 1).coerceAtLeast(0))
            } else {
                listState.scrollToItem((value - range.first).coerceIn(0, itemCount - 1))
            }
        }
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .height(itemHeight * 3)
                .nestedScroll(consumeLeftoverScrollConnection),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                modifier = Modifier.height(itemHeight * 3),
                contentPadding = if (loop) PaddingValues(0.dp) else PaddingValues(vertical = itemHeight)
            ) {
                if (loop) {
                    items(count = Int.MAX_VALUE, key = { index -> index }) { index ->
                        val actualIndex = (index % itemCount + itemCount) % itemCount
                        WheelItem(formatValue(items[actualIndex]), actualIndex == selectedIndex, itemHeight)
                    }
                } else {
                    items(count = itemCount, key = { index -> index }) { index ->
                        WheelItem(formatValue(items[index]), index == selectedIndex, itemHeight)
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight * 0.8f)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.background.copy(alpha = 0f)
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight * 0.8f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = 0f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun WheelItem(text: String, isSelected: Boolean, itemHeight: Dp) {
    val fontSizeAnimated by animateFloatAsState(
        targetValue = if (isSelected) 40f else 28f,
        animationSpec = tween(150),
        label = "FontSize"
    )
    Box(
        modifier = Modifier.height(itemHeight).fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = fontSizeAnimated.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

private val consumeLeftoverScrollConnection = object : NestedScrollConnection {
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset = available // claim whatever the wheel didn't use, so the screen never sees it

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = available
}