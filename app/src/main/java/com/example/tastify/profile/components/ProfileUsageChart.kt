package com.example.tastify.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun ProfileUsageCard(
    totalUsageMinutes: Int,
    averageDailyUsageMinutes: Int,
    weeklyUsageMinutes: List<Int>,
    usageAccessGranted: Boolean,
    onOpenUsageAccessSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("App usage time", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
    }
    Spacer(modifier = Modifier.height(12.dp))
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                UsagePeriodToggle()
            }
            Spacer(modifier = Modifier.height(24.dp))
            if (usageAccessGranted) {
                UsageChart(weeklyUsageMinutes)
            } else {
                UsageAccessPrompt(onOpenUsageAccessSettings)
            }
            Spacer(modifier = Modifier.height(24.dp))
            UsageSummaryRow(
                totalUsageMinutes = totalUsageMinutes,
                averageDailyUsageMinutes = averageDailyUsageMinutes,
                todayUsageMinutes = weeklyUsageMinutes.getOrNull(LocalDate.now().dayOfWeek.value - 1) ?: 0
            )
        }
    }
}

@Composable
private fun UsagePeriodToggle() {
    Surface(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(
                "this week",
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun UsageChart(weeklyUsageMinutes: List<Int>) {
    val currentDayIndex = remember { LocalDate.now().dayOfWeek.value - 1 }
    val values = remember(weeklyUsageMinutes) {
        if (weeklyUsageMinutes.size == 7) weeklyUsageMinutes else List(7) { 0 }
    }
    val maxMinutes = remember(values) {
        values.maxOrNull()?.coerceAtLeast(1) ?: 1
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

        days.forEachIndexed { index, day ->
            val isToday = index == currentDayIndex
            val minutes = values[index].coerceAtLeast(0)
            val height = minutes.toFloat() / maxMinutes.toFloat()
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .fillMaxHeight(if (minutes > 0) height.coerceIn(0.04f, 1f) else 0.04f)
                            .background(
                                if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = day,
                    fontSize = 10.sp,
                    color = if (isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun UsageSummaryRow(
    totalUsageMinutes: Int,
    averageDailyUsageMinutes: Int,
    todayUsageMinutes: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SummaryMetric(label = "Total", value = formatDuration(totalUsageMinutes))
        SummaryMetric(label = "Daily avg", value = formatDuration(averageDailyUsageMinutes))
        SummaryMetric(label = "Today", value = formatDuration(todayUsageMinutes))
    }
}

@Composable
private fun UsageAccessPrompt(onOpenUsageAccessSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Usage access required", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Android needs permission to read app usage time", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onOpenUsageAccessSettings) {
            Text("Open settings")
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}

private fun formatDuration(totalMinutes: Int): String {
    val safeMinutes = totalMinutes.coerceAtLeast(0)
    val hours = safeMinutes / 60
    val minutes = safeMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}