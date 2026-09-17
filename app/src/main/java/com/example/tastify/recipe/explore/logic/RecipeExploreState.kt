package com.example.tastify.recipe.explore.logic

import com.example.tastify.models.CuisineType
import com.example.tastify.models.DietaryRestriction
import com.example.tastify.models.Recipe
import com.example.tastify.models.RecipeDifficulty

data class RecipeExploreState(
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isFiltering: Boolean = false,
    val recipes: List<Recipe> = emptyList(),
    val favoriteRecipeIds: Set<String> = emptySet(),

    // Filtri
    val selectedDifficulty: RecipeDifficulty? = null,
    val servings: Int = 1,
    val costRange: ClosedFloatingPointRange<Float> = 1f..5f,
    val maxCalories: Float = 5000f,
    val selectedDietaryRestrictions: Set<DietaryRestriction> = emptySet(),
    val selectedCuisineTypes: Set<CuisineType> = emptySet(),
    val includedIngredients: Set<String> = emptySet()
)
