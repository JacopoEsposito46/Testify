package com.example.tastify.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileTagSection(title: String, tags: List<String>) {
    var isExpanded by remember { mutableStateOf(false) }
    val displayTags = if (isExpanded || tags.size <= 5) tags else tags.take(5)
    val hasMore = tags.size > 5

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            if (hasMore) {
                Text(
                    text = if (isExpanded) "See less" else "See all",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .clickable { isExpanded = !isExpanded }
                        .padding(8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (title.contains("Dietary/Restriction") && tags.isEmpty()){
            Text(
                text = "No dietary restrictions selected",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
            )
        }else if (title.contains("Kitchen styles") && tags.isEmpty()){
            Text(
                text = "No kitchen styles selected",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                displayTags.forEach { tag ->
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 2.dp, vertical = 2.dp),
                        shape = CircleShape,
                        border = BorderStroke(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.secondary
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = if(title.contains("Kitchen styles"))Icons.Filled.Restaurant else Icons.Filled.Sell,
                                contentDescription = if(title.contains("Kitchen styles"))"cuisine" else "difficulty",
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}