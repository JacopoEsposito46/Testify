package com.example.tastify.recipe.weekly.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.tastify.plan.models.DayDate

@Composable
fun DaySelectorRow(
    days: List<DayDate>,
    onDaySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
    ) {
        items(days) { day ->
            DayItem(
                day = day,
                onClick = { onDaySelected(day.dayNumber) }
            )
        }
    }
}

@Composable
private fun DayItem(
    day: DayDate,
    onClick: () -> Unit
) {
    val backgroundColor = if (day.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (day.isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val borderColor = if (day.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary

    Column(
        modifier = Modifier
            .width(60.dp)
            .height(85.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = day.dayName,
            style = MaterialTheme.typography.titleMedium,
            color = contentColor
        )
        Text(
            text = day.dayNumber,
            style = MaterialTheme.typography.headlineMedium,
            color = contentColor
        )
        if (day.isSelected) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.onPrimary)
            )
        } else {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
