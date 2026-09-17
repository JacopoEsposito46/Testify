package com.example.tastify.recipe.weekly.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.Fastfood
import androidx.compose.material.icons.outlined.ModeNight
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastify.plan.models.MealSlot

@Composable
fun MealSection(
    mealSlot: MealSlot,
    onAddRecipe: (String) -> Unit,
    onRemoveRecipe: (String, String) -> Unit,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (mealSlot.type.lowercase()) {
                    "breakfast" -> Icons.Outlined.WbTwilight
                    "lunch" -> Icons.Filled.WbSunny
                    "snack" -> Icons.Outlined.Fastfood
                    else -> Icons.Outlined.ModeNight
                }
                Icon(
                    imageVector = icon,
                    contentDescription = mealSlot.type,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = mealSlot.type,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = mealSlot.time,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (mealSlot.recipes.isEmpty()) {
            EmptyMealCard(
                mealName = mealSlot.type,
                onClick = { onAddRecipe(mealSlot.type) }
            )
        } else {
            mealSlot.recipes.forEachIndexed { index, recipe ->
                key("${recipe.id}_$index") {
                    SwipeToDismissRecipeBox(
                        recipe = recipe,
                        onDismiss = { onRemoveRecipe(mealSlot.type, recipe.id) },
                        onRecipeClick = { onRecipeClick(recipe.id) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            EmptyMealCard(
                mealName = mealSlot.type,
                onClick = { onAddRecipe(mealSlot.type) },
                isAddAnother = true
            )
        }
    }
}
