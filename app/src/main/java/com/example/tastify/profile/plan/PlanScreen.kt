package com.example.tastify.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tastify.plan.components.EmptyMealCard
import com.example.tastify.plan.components.GenerateAiButton
import com.example.tastify.plan.components.MealSectionHeader
import com.example.tastify.plan.components.PlanRecipeCard
import com.example.tastify.plan.components.WeeklyCalendarStrip
import com.example.tastify.plan.models.DayDate
import com.example.tastify.plan.models.MealSlot

@Composable
fun PlanScreen(
    days: List<DayDate>,
    mealSlots: List<MealSlot>,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDaySelected: (DayDate) -> Unit,
    onAddRecipeClick: (MealSlot) -> Unit,
    onRecipeClick: (String) -> Unit,
    onGenerateAiClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 4.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Weekly Plan",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Center)
                )

                TextButton(
                    onClick = onEditClick,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text(
                        text = "Edit",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                item {
                    Text(
                        text = "Organize your meals for the week",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    WeeklyCalendarStrip(
                        days = days,
                        onDaySelected = onDaySelected
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                items(mealSlots) { slot ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp)
                    ) {
                        MealSectionHeader(
                            time = slot.time,
                            title = slot.type,
                            calories = slot.totalCalories,
                            onAddClick = { onAddRecipeClick(slot) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (slot.recipes.isEmpty()) {
                            EmptyMealCard(
                                onClick = { onAddRecipeClick(slot) }
                            )
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(slot.recipes) { recipe ->
                                    PlanRecipeCard(
                                        recipe = recipe,
                                        onClick = { onRecipeClick(recipe.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                GenerateAiButton(
                    onClick = onGenerateAiClick
                )
            }
        }
    }
}