package com.sda5.clockapp.worldclock

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.time.ZonedDateTime


data class WorldClock(
    val city : String,
    val country: String,
    val timeZone: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldClockScreen(
    onAddCity: () -> Unit,
    modifier: Modifier = Modifier
) {
    val application = LocalContext.current.applicationContext as ClockApplication
    val viewModel: WorldClockViewModel = viewModel(factory = WorldClockViewModelFactory(application))
    val cities by viewModel.cities.collectAsState()

    var now by remember { mutableStateOf(ZonedDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = ZonedDateTime.now()
            delay(1000L)
        }
    }

    WorldClockContent(
        cities = cities,
        now = now,
        onAddCity = onAddCity,
        onDeleteCity = { viewModel.deleteCity(it) },
        modifier = modifier
    )
}

@Composable
fun WorldClockContent(
    cities: List<WorldClockCity>,
    now: ZonedDateTime,
    onAddCity: () -> Unit,
    onDeleteCity: (WorldClockCity) -> Unit,
    modifier: Modifier = Modifier
) {
    val localZoneId = remember { ZoneId.systemDefault().id }
    val localCity = remember(localZoneId) { formattedRegion(localZoneId) }
    val localInfo = remember(localZoneId, now) { cityTimeInfo(localZoneId, now) }

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
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$localCity  ${localInfo.timeText}",
//                    text = "All TimeZone",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(onClick = onAddCity) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add City",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (cities.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
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
                        CityCard(city = city, now = now, onDelete = { onDeleteCity(city) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CityCard(city: WorldClockCity, now: ZonedDateTime, onDelete: () -> Unit) {
    val info = remember(city.zoneId, now) { cityTimeInfo(city.zoneId, now) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = {}, onLongClick = onDelete),
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

@Preview(showBackground = true)
@Composable
private fun WorldClockContentPreview() {
    MaterialTheme {
        WorldClockContent(
            cities = listOf(
                WorldClockCity(id = 1L, zoneId = "America/New_York", displayName = "New York"),
                WorldClockCity(id = 2L, zoneId = "Europe/London", displayName = "London"),
                WorldClockCity(id = 3L, zoneId = "Asia/Tokyo", displayName = "Tokyo")
            ),
            now = ZonedDateTime.now(),
            onAddCity = {},
            onDeleteCity = {}
        )
    }
}

/*
@Preview(showBackground = true)
@Composable
private fun WorldClockContentEmptyPreview() {
    MaterialTheme {
        WorldClockContent(
            cities = emptyList(),
            now = ZonedDateTime.now(),
            onAddCity = {},
            onDeleteCity = {}
        )
    }
}*/
