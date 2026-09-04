package com.sda5.clockapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sda5.clockapp.ui.theme.ClockBackground
import com.sda5.clockapp.ui.theme.ClockMutedText
import com.sda5.clockapp.ui.theme.GlassBorder
import com.sda5.clockapp.ui.theme.GlassSurface
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
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        // Selection glass highlight pill in background
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(64.dp)
                .padding(top = 18.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(GlassSurface)
                .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
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
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 28.dp)
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
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 28.dp)
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelColumn(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 60.dp
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
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(bottom = 8.dp)
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

                    val fontSizeAnimated by animateFloatAsState(
                        targetValue = if (isSelected) 44f else 32f,
                        animationSpec = tween(150),
                        label = "FontSize"
                    )

                    Box(
                        modifier = Modifier
                            .height(itemHeight)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%02d", itemValue),
                            fontSize = fontSizeAnimated.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else ClockMutedText.copy(alpha = 0.3f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Top and Bottom gradient fade masks
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight * 0.8f)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(ClockBackground, Color.Transparent)
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
                            colors = listOf(Color.Transparent, ClockBackground)
                        )
                    )
            )
        }
    }
}

