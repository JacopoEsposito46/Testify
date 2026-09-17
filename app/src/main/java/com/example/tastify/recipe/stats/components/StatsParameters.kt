package com.example.tastify.recipe.stats.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tastify.ui.theme.TastifyTheme

@Composable
fun StatsParameters(
    totalFavorites: Int = 0,
    totalComments: Int = 0,
    totalReviews: Int = 0,
    averageRating: Double = 0.0,
    favoritesChangeText: String = "+0% vs yesterday",
    commentsChangeText: String = "+0% vs yesterday",
    reviewsChangeText: String = "+0% vs yesterday"
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        maxItemsInEachRow = 2
    ) {
        StatParameterCard(
            label = "FAVORITES",
            value = "%,d".format(totalFavorites),
            supportingText = favoritesChangeText,
            icon = Icons.Default.FavoriteBorder,
            modifier = Modifier.weight(1f)
        )

        StatParameterCard(
            label = "COMMENTS",
            value = "%,d".format(totalComments),
            supportingText = commentsChangeText,
            icon = Icons.AutoMirrored.Default.Chat,
            modifier = Modifier.weight(1f)
        )

        StatParameterCard(
            label = "REVIEWS",
            value = "%,d".format(totalReviews),
            supportingText = reviewsChangeText,
            icon = Icons.Default.RateReview,
            modifier = Modifier.weight(1f)
        )

        StatParameterCard(
            label = "RATING",
            value = "%.1f".format(averageRating),
            supportingText = "Trend unavailable",
            icon = Icons.Default.Star,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatParameterCard(
    label: String,
    value: String,
    supportingText: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.heightIn(min = 110.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(),
        border = BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.secondary
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = supportingText,
                maxLines = 2,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ParametersPreview(){
    TastifyTheme{
        StatsParameters()
    }
}