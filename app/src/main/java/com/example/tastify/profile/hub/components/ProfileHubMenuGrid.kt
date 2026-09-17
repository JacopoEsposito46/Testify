package com.example.tastify.profile.hub.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileHubMenuGrid(
    onNavigateToHistory: () -> Unit,
    onNavigateToPersonalInfo: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToPlanning: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            HubMenuCard(
                icon = Icons.Outlined.History,
                label = "History",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToHistory
            )
            HubMenuCard(
                icon = Icons.Outlined.Person,
                label = "Personal info",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToPersonalInfo
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            HubMenuCard(
                icon = Icons.Outlined.BarChart,
                label = "Analytics",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToAnalytics
            )
            HubMenuCard(
                icon = Icons.Outlined.CalendarMonth,
                label = "Planning",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToPlanning
            )
        }
    }
}
