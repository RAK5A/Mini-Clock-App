package com.sda5.clockapp.worldclock

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

val allCities = listOf(
    WorldClock("New York", "United States", "America/New_York"),
    WorldClock("Los Angeles", "United States", "America/Los_Angeles"),
    WorldClock("London", "United Kingdom", "Europe/London"),
    WorldClock("Paris", "France", "Europe/Paris"),
    WorldClock("Tokyo", "Japan", "Asia/Tokyo"),
    WorldClock("Dubai", "UAE", "Asia/Dubai"),
    WorldClock("Sydney", "Australia", "Australia/Sydney"),
    WorldClock("Singapore", "Singapore", "Asia/Singapore"),
    WorldClock("Phnom Penh", "Cambodia", "Asia/Phnom_Penh"),
    WorldClock("Bangkok", "Thailand", "Asia/Bangkok"),
    WorldClock("Seoul", "South Korea", "Asia/Seoul"),
    WorldClock("Berlin", "Germany", "Europe/Berlin"),
    WorldClock("Moscow", "Russia", "Europe/Moscow"),
    WorldClock("Cairo", "Egypt", "Africa/Cairo"),
    WorldClock("Mumbai", "India", "Asia/Kolkata"),
    WorldClock("Toronto", "Canada", "America/Toronto"),
    WorldClock("São Paulo", "Brazil", "America/Sao_Paulo"),
    WorldClock("Johannesburg", "South Africa", "Africa/Johannesburg"),
    WorldClock("Istanbul", "Turkey", "Europe/Istanbul"),
    WorldClock("Beijing", "China", "Asia/Shanghai")
)

@Composable
fun SearchClockScreen(
    onCityAdded: (WorldClock) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredCities = remember(searchQuery) {
        if (searchQuery.isEmpty()) allCities
        else allCities.filter {
            it.city.contains(searchQuery, ignoreCase = true) ||
                    it.country.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF2F4F8)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {


            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = Color(0xFF1A1A2E),
                    modifier = Modifier
                        .clickable { onBack() }
                        .padding(end = 12.dp)
                )
                Text(
                    "Add City",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search city or country...", color = Color.Gray) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.clickable { searchQuery = "" }
                        )
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1A1A2E),
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))


            Text(
                "${filteredCities.size} cities found",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            // City list
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(filteredCities) { clock ->
                    SearchCityCard(
                        clock = clock,
                        onAdd = {
                            onCityAdded(clock)
                            onBack()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchCityCard(
    clock: WorldClock,
    onAdd: () -> Unit
) {
    val timeZone = TimeZone.getTimeZone(clock.timeZone)
    var time by remember { mutableStateOf(getFormattedTime(timeZone)) }

      LaunchedEffect(Unit) {
        while (true) {
            time = getFormattedTime(timeZone)
            kotlinx.coroutines.delay(1000)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column {
                Text(clock.city, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                Text(clock.country, fontSize = 12.sp, color = Color.Gray)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(time, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A2E))
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onAdd() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AddCircle,
                        contentDescription = "Add city",
                        tint = Color(0xFF1A1A2E),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

private fun getFormattedTime(timeZone: TimeZone): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    sdf.timeZone = timeZone
    return sdf.format(Date())
}