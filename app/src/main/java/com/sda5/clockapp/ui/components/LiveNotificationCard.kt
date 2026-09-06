//package com.sda5.clockapp.ui.components
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.HourglassBottom
//import androidx.compose.material.icons.filled.Pause
//import androidx.compose.material.icons.filled.PlayArrow
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.sda5.clockapp.ui.theme.ClockLiveIconBg
//import com.sda5.clockapp.ui.theme.ClockLiveNotificationBg
//import com.sda5.clockapp.ui.theme.ClockMutedText
//
//@Composable
//fun LiveNotificationCard(
//    remainingDisplay: String,
//    totalDisplay: String,
//    targetFinishTime: String,
//    isRunning: Boolean,
//    onPauseResumeToggle: () -> Unit,
//    onClose: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Column(
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = 8.dp)
//    ) {
//        Text(
//            text = "Live notifications",
//            color = Color.White.copy(alpha = 0.9f),
//            fontSize = 18.sp,
//            fontWeight = FontWeight.SemiBold,
//            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
//        )
//
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .clip(RoundedCornerShape(32.dp))
//                .background(ClockLiveNotificationBg)
//                .padding(horizontal = 16.dp, vertical = 14.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Hourglass Icon Badge
//            Box(
//                modifier = Modifier
//                    .size(52.dp)
//                    .clip(RoundedCornerShape(18.dp))
//                    .background(ClockLiveIconBg),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    imageVector = Icons.Filled.HourglassBottom,
//                    contentDescription = "Timer",
//                    tint = Color.White,
//                    modifier = Modifier.size(28.dp)
//                )
//            }
//
//            Spacer(modifier = Modifier.width(16.dp))
//
//            // Time & Subtext
//            Column(
//                modifier = Modifier.weight(1f),
//                verticalArrangement = Arrangement.Center
//            ) {
//                Text(
//                    text = remainingDisplay,
//                    color = Color.White,
//                    fontSize = 32.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    letterSpacing = 0.5.sp
//                )
//
//                Text(
//                    text = "$totalDisplay / $targetFinishTime",
//                    color = ClockMutedText.copy(alpha = 0.9f),
//                    fontSize = 14.sp,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//
//            // Right Controls (X and || / >)
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(4.dp)
//            ) {
//                IconButton(onClick = onClose) {
//                    Icon(
//                        imageVector = Icons.Filled.Close,
//                        contentDescription = "Close Notification",
//                        tint = Color.White.copy(alpha = 0.8f),
//                        modifier = Modifier.size(24.dp)
//                    )
//                }
//
//                IconButton(onClick = onPauseResumeToggle) {
//                    Icon(
//                        imageVector = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
//                        contentDescription = if (isRunning) "Pause" else "Resume",
//                        tint = Color.White,
//                        modifier = Modifier.size(26.dp)
//                    )
//                }
//            }
//        }
//    }
//}
