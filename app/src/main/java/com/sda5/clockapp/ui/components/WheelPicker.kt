package com.sda5.clockapp.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sda5.clockapp.ui.theme.ClockMutedText
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hours wheel
        WheelColumn(
            label = "Hours",
            value = hours,
            range = 0..99,
            onValueChange = onHoursChange,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = ":",
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 28.dp, start = 4.dp, end = 4.dp)
        )

        // Minutes wheel
        WheelColumn(
            label = "Minutes",
            value = minutes,
            range = 0..59,
            onValueChange = onMinutesChange,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = ":",
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 28.dp, start = 4.dp, end = 4.dp)
        )

        // Seconds wheel
        WheelColumn(
            label = "Seconds",
            value = seconds,
            range = 0..59,
            onValueChange = onSecondsChange,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelColumn(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 56.dp
) {
    val items = remember(range) { range.toList() }
    val itemCount = items.size

    val initialIndex = remember(value, itemCount) {
        val middleLoop = 1000 * itemCount
        middleLoop + (value - range.first)
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (initialIndex - 1).coerceAtLeast(0))
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    val selectedIndex by remember {
        derivedStateOf {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            if (offset > itemHeight.value / 2) {
                (firstVisibleIndex + 2) % itemCount
            } else {
                (firstVisibleIndex + 1) % itemCount
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { selectedIndex }
            .distinctUntilChanged()
            .collect { index ->
                val newValue = range.first + index
                onValueChange(newValue)
            }
    }

    LaunchedEffect(value) {
        val currentMappedVal = range.first + selectedIndex
        if (currentMappedVal != value) {
            val targetIndex = 1000 * itemCount + (value - range.first)
            listState.scrollToItem((targetIndex - 1).coerceAtLeast(0))
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = ClockMutedText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Box(
            modifier = Modifier.height(itemHeight * 3),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                modifier = Modifier.height(itemHeight * 3)
            ) {
                items(
                    count = Int.MAX_VALUE,
                    key = { index -> index }
                ) { index ->
                    val actualIndex = (index % itemCount + itemCount) % itemCount
                    val itemValue = items[actualIndex]
                    val isSelected = actualIndex == selectedIndex

                    Box(
                        modifier = Modifier
                            .height(itemHeight)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%02d", itemValue),
                            fontSize = if (isSelected) 46.sp else 36.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else ClockMutedText.copy(alpha = 0.35f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
