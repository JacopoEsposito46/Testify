package com.example.tastify.recipe.stats.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.tastify.recipe.stats.logic.RecipeStatsPeriod
import com.example.tastify.recipe.stats.logic.RecipeTrendPoint
import com.example.tastify.ui.theme.TastifyTheme

@Composable
fun WeeklyTrend(
    selectedPeriod: RecipeStatsPeriod = RecipeStatsPeriod.WEEK,
    trendPoints: List<RecipeTrendPoint> = emptyList(),
    onPeriodSelected: (RecipeStatsPeriod) -> Unit = {}
){

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface

        ),
        elevation = CardDefaults.cardElevation(),
        border = BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.secondary
        )
    ){
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                //Title
                Text(
                    text = if (selectedPeriod == RecipeStatsPeriod.WEEK) "Weekly trend" else "6-month trend",
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                //Toggle Week/Month
                CustomToggle(
                    selectedOption = selectedPeriod,
                    onOptionSelected = onPeriodSelected
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // histogram
            BarChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                trendPoints = trendPoints
            )
        }
    }
}

@Composable
fun CustomToggle(
    selectedOption: RecipeStatsPeriod,
    onOptionSelected: (RecipeStatsPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 3.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (selectedOption == RecipeStatsPeriod.WEEK) MaterialTheme.colorScheme.onPrimary
                    else Color.Transparent
                )
                .width(56.dp)
                .clickable { onOptionSelected(RecipeStatsPeriod.WEEK) }
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "week",
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selectedOption == RecipeStatsPeriod.WEEK) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (selectedOption == RecipeStatsPeriod.MONTH) MaterialTheme.colorScheme.onPrimary
                    else Color.Transparent
                )
                .width(56.dp)
                .clickable { onOptionSelected(RecipeStatsPeriod.MONTH) }
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "month",
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selectedOption == RecipeStatsPeriod.MONTH) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    trendPoints: List<RecipeTrendPoint>
) {
    val data = if (trendPoints.isEmpty()) {
        List(7) { RecipeTrendPoint(label = "", value = 0) }
    } else {
        trendPoints
    }
    val maxValue = data.maxOfOrNull { it.value }?.takeIf { it > 0 } ?: 1
    val isMonthly = data.size == 6

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val lineYPositions = listOf(0f, canvasHeight * 0.33f, canvasHeight * 0.66f, canvasHeight)

                lineYPositions.forEach { y ->
                    drawLine(
                        color = Color(0xFFE0E0E0),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxSize()) {
                data.forEachIndexed { index, point ->
                    val normalizedValue = point.value.toFloat() / maxValue.toFloat()
                    val isCurrentPeriod = index == data.lastIndex
                    val barHeight = when {
                        point.value > 0 -> normalizedValue.coerceAtLeast(0.03f)
                        isCurrentPeriod -> 0.03f
                        else -> 0f
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .width(if (isMonthly) 18.dp else 16.dp)
                                .fillMaxHeight(barHeight)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(
                                    if (isCurrentPeriod) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.secondary
                                    }
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            data.forEach { point ->
                Text(
                    text = point.label,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeeklyTrendPreview(){
    TastifyTheme{
        WeeklyTrend()
    }
}
