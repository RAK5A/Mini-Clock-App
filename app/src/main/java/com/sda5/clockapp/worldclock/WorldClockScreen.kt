package com.sda5.clockapp.worldclock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sda5.clockapp.ClockApplication
import com.sda5.clockapp.model.WorldClockCity
import com.sda5.clockapp.ui.components.SelectableRow
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
fun WorldClockScreen(
    onAddCity: () -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ClockApplication
    val viewModel: WorldClockViewModel = viewModel(factory = WorldClockViewModelFactory(application))
    val cities by viewModel.cities.collectAsState()
    var selectedIds by remember { mutableStateOf(setOf<Long>()) }
    val isSelectionMode = selectedIds.isNotEmpty()

    var now by remember { mutableStateOf(ZonedDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = ZonedDateTime.now()
            delay(1000L)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSelectionMode) {
                    Text(
                        text = "${selectedIds.size} selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row {
                        IconButton(onClick = { selectedIds = emptySet() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cancel selection", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        IconButton(onClick = {
                            viewModel.deleteCities(selectedIds)
                            selectedIds = emptySet()
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete selected", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                } else {
                    val localZoneId = ZoneId.systemDefault().id
                    val localInfo = remember(now) { cityTimeInfo(localZoneId, now) }
                    Column {
                        Text("Local", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${formattedCityName(localZoneId)} · ${localInfo.timeText}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onAddCity) {
                        Icon(Icons.Filled.Add, contentDescription = "Add city", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (cities.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No cities added",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(cities, key = { it.id }) { city ->
                        SelectableRow(
                            isSelectionMode = isSelectionMode,
                            isSelected = city.id in selectedIds,
                            onToggleSelected = {
                                selectedIds = if (city.id in selectedIds) selectedIds - city.id else selectedIds + city.id
                            },
                            onClick = {},
                            onLongClick = { selectedIds = setOf(city.id) }
                        ) {
                            CityCard(city = city, now = now)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CityCard(city: WorldClockCity, now: ZonedDateTime) {
    val info = remember(city.zoneId, now) { cityTimeInfo(city.zoneId, now) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = city.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "${info.dayLabel} · ${info.utcOffsetLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = info.timeText,
                fontFamily = FontFamily.Monospace,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
