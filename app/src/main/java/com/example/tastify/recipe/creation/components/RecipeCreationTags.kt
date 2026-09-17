package com.example.tastify.recipe.creation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tastify.models.CuisineType
import com.example.tastify.models.DietaryRestriction
import com.example.tastify.models.Recipe
import com.example.tastify.profile.components.ToggleOption

@Composable
fun RecipeCreationTags(
    recipe: Recipe,
    onValueChange: (Recipe) -> Unit = {}
){

    val allCuisines = CuisineType.entries
    var isCuisineExpanded by remember { mutableStateOf(false) }
    val sortedCuisines = remember(recipe.cuisineType, allCuisines) {
        allCuisines.sortedByDescending { it == recipe.cuisineType }
    }
    val visibleCuisinesTags = if (isCuisineExpanded) sortedCuisines else sortedCuisines.take(4)

    val allDietary = DietaryRestriction.entries
    var isDietaryExpanded by remember { mutableStateOf(false) }
    val sortedDietary = remember(recipe.dietaryRestrictions, allDietary) {
        allDietary.sortedByDescending { recipe.dietaryRestrictions.contains(it) }
    }
    val visibleDietaryTags = if (isDietaryExpanded) sortedDietary else sortedDietary.take(4)

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ){
        Text(
            text = "Cuisine tags",
            style = MaterialTheme.typography.titleMedium
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            maxItemsInEachRow = 2
        ) {
            visibleCuisinesTags.forEach { cuisine ->
                val isChecked = recipe.cuisineType == cuisine
                ToggleOption(
                    modifier = Modifier
                        .weight(1f),
                    label = cuisine.displayName,
                    checked = isChecked,
                    onCheckedChange = { checked ->
                        if(checked){
                            onValueChange(recipe.copy(cuisineType = cuisine))
                        } else {
                            onValueChange(recipe.copy(cuisineType = CuisineType.DEFAULT))
                        }
                    }
                )
            }
            if (visibleCuisinesTags.size % 2 != 0) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Text(
            text = if (isCuisineExpanded) "Show less" else "Expand all",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable { isCuisineExpanded = !isCuisineExpanded }
                .padding(8.dp)
        )

        Text(
            text = "Dietary restriction tags",
            style = MaterialTheme.typography.titleMedium
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            maxItemsInEachRow = 2
        ){
            visibleDietaryTags.forEach { dietary ->
                val isChecked = recipe.dietaryRestrictions.contains(dietary)
                ToggleOption(
                    modifier = Modifier
                        .weight(1f),
                    label = dietary.displayName,
                    checked = isChecked,
                    onCheckedChange = { checked ->
                        val newRestrictions = if(checked){
                            recipe.dietaryRestrictions + dietary
                        } else {
                            recipe.dietaryRestrictions - dietary
                        }
                        onValueChange(recipe.copy(dietaryRestrictions = newRestrictions))
                    }
                )
            }
            if (visibleDietaryTags.size % 2 != 0) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Text(
            text = if (isDietaryExpanded) "Show less" else "Expand all",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable { isDietaryExpanded = !isDietaryExpanded }
                .padding(8.dp)
        )
    }
}