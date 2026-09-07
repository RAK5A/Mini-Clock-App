package com.sda5.clockapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectableRow(
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onToggleSelected: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier.combinedClickable(
            onClick = { if (isSelectionMode) onToggleSelected() else onClick() },
            onLongClick = onLongClick
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
            exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isSelected, onCheckedChange = { onToggleSelected() })
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}