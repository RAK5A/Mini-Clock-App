package com.sda5.clockapp.worldclock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*


data class WorldClock(
    val city : String,
    val country: String,
    val timeZone: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldClockScreen(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit = {}
) {
    val clocks = listOf(
        WorldClock("New York", "United States", "America/New_York"),
        WorldClock("London", "United Kingdom", "Europe/London"),
        WorldClock("Tokyo", "Japan", "Asia/Tokyo"),
        WorldClock("Dubai", "UAE", "Asia/Dubai"),
        WorldClock("Paris", "France", "Europe/Paris"),
        WorldClock("Sydney", "Australia", "Australia/Sydney"),
        WorldClock("Phnom Penh", "Cambodia", "Asia/Phnom_Penh")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("World Clock", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = Color(0xFF1A1A2E),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add City")
            }
        },
        containerColor = Color(0xFFF2F4F8)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                LocalTimeCard()
            }
            item {
                Text(
                    "Other Cities",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(clocks) { clock ->
                WorldClockCard(clock)
            }
        }
    }
}

@Composable
fun LocalTimeCard() {
    val timeZone = TimeZone.getDefault()
    var time by remember { mutableStateOf(getFormattedTime(timeZone)) }

    LaunchedEffect(Unit) {
        while (true) {
            time = getFormattedTime(timeZone)
            kotlinx.coroutines.delay(1000)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Local Time", color = Color.Gray, fontSize = 14.sp)
            Text(
                time,
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                timeZone.id,
                color = Color.LightGray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun WorldClockCard(clock: WorldClock) {
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
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(clock.city, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(clock.country, fontSize = 14.sp, color = Color.Gray)
            }
            Text(
                time,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A2E)
            )
        }
    }
}

private fun getFormattedTime(timeZone: TimeZone): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    sdf.timeZone = timeZone
    return sdf.format(Date())
}