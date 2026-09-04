package com.sda5.clockapp.worldclock

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sda5.clockapp.ui.theme.ClockBackground
import com.sda5.clockapp.ui.theme.ClockMutedText
import com.sda5.clockapp.ui.theme.ClockSurfaceElevated
import com.sda5.clockapp.ui.theme.GlassBorder
import com.sda5.clockapp.ui.theme.WorldClockAccent
import com.sda5.clockapp.ui.theme.WorldClockGradientBrush

data class WorldCity(
    val id: String,
    val name: String,
    val country: String,
    val time: String,
    val amPm: String,
    val timeDiff: String,
    val dayOfWeek: String,
    val isDaytime: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldClockScreen(modifier: Modifier = Modifier) {
    val cities = remember {
        listOf(
            WorldCity("1", "Phnom Penh", "Cambodia (Local)", "08:30", "PM", "Same time", "Today", false),
            WorldCity("2", "London", "United Kingdom", "02:30", "PM", "-6 hrs", "Today", true),
            WorldCity("3", "New York", "United States", "09:30", "AM", "-11 hrs", "Today", true),
            WorldCity("4", "Tokyo", "Japan", "10:30", "PM", "+2 hrs", "Today", false),
            WorldCity("5", "Paris", "France", "03:30", "PM", "-5 hrs", "Today", true),
            WorldCity("6", "Sydney", "Australia", "11:30", "PM", "+3 hrs", "Today", false)
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ClockBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "World Clock",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ClockBackground
                ),
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Cities",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = WorldClockAccent,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add City",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                LocalClockHeroCard(city = cities.first())
            }

            item {
                Text(
                    text = "WORLD CITIES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ClockMutedText,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 10.dp)
                )
            }

            items(cities.drop(1), key = { it.id }) { city ->
                WorldCityCard(city = city)
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun LocalClockHeroCard(city: WorldCity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(WorldClockGradientBrush)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LOCAL TIME",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = city.name,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = city.country,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp
            )
        }

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = city.time,
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = city.amPm,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }
}

@Composable
private fun WorldCityCard(city: WorldCity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ClockSurfaceElevated)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (city.isDaytime) Icons.Default.WbSunny else Icons.Default.NightsStay,
                    contentDescription = null,
                    tint = if (city.isDaytime) Color(0xFFFBBF24) else Color(0xFFA7F3D0),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${city.dayOfWeek}, ${city.timeDiff}",
                    fontSize = 13.sp,
                    color = ClockMutedText,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = city.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = city.country,
                fontSize = 13.sp,
                color = ClockMutedText
            )
        }

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = city.time,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = city.amPm,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = WorldClockAccent,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}