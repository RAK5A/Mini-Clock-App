package com.sda5.clockapp.timer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sda5.clockapp.model.PresetTime

@Composable
fun PresetsRow(
    presets: List<PresetTime>,
    currentHours: Int,
    currentMinutes: Int,
    currentSeconds: Int,
    onSelectPreset: (PresetTime) -> Unit,
    onAddCurrentAsPreset: () -> Unit,
    onDeleteSelected: (Set<String>) -> Unit,
    isCurrentValid: Boolean
) {
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    val isSelectionMode = selectedIds.isNotEmpty()

    Column {
        AnimatedVisibility(visible = isSelectionMode, enter = fadeIn(), exit = fadeOut()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedIds.size} selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    IconButton(onClick = { selectedIds = emptySet() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Cancel selection")
                    }
                    IconButton(onClick = {
                        onDeleteSelected(selectedIds)
                        selectedIds = emptySet()
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete selected preset", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(presets, key = { it.id }) { preset ->
                val isActive = preset.hours == currentHours &&
                        preset.minutes == currentMinutes &&
                        preset.seconds == currentSeconds
                PresetChip(
                    preset = preset,
                    isActive = isActive,
                    isSelectionMode = isSelectionMode,
                    isChecked = preset.id in selectedIds,
                    onClick = { onSelectPreset(preset) },
                    onToggleChecked = {
                        selectedIds = if (preset.id in selectedIds) selectedIds - preset.id else selectedIds + preset.id
                    },
                    onLongClick = { selectedIds = setOf(preset.id) }
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            if (isCurrentValid && !isSelectionMode) {
                item {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .combinedClickable(onClick = onAddCurrentAsPreset),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Save Preset",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
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
    isActive: Boolean,
    isSelectionMode: Boolean,
    isChecked: Boolean,
    onClick: () -> Unit,
    onToggleChecked: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
            )
            .combinedClickable(
                onClick = { if (isSelectionMode) onToggleChecked() else onClick() },
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
            exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isChecked, onCheckedChange = { onToggleChecked() })
                Spacer(modifier = Modifier.width(6.dp))
            }
        }
        Text(
            text = preset.formattedString,
            color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}