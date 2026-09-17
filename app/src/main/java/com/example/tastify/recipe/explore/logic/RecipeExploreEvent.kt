package com.example.tastify.recipe.explore.logic

import com.example.tastify.models.CuisineType
import com.example.tastify.models.DietaryRestriction
import com.example.tastify.models.RecipeDifficulty

sealed interface RecipeExploreEvent {
    data class UpdateSearchQuery(val newQuery: String) : RecipeExploreEvent
    data class UpdateDifficulty(val difficulty: RecipeDifficulty?) : RecipeExploreEvent
    data class UpdateServings(val servings: Int) : RecipeExploreEvent
    data class UpdateCostRange(val range: ClosedFloatingPointRange<Float>) : RecipeExploreEvent
    data class UpdateMaxCalories(val calories: Float) : RecipeExploreEvent
    data class ToggleDietaryRestriction(val restriction: DietaryRestriction) : RecipeExploreEvent
    data class ToggleCuisineType(val cuisineType: CuisineType) : RecipeExploreEvent
    data class AddIngredient(val ingredient: String) : RecipeExploreEvent
    data class RemoveIngredient(val ingredient: String) : RecipeExploreEvent
    data class ToggleFavourite(val recipeId: String) : RecipeExploreEvent
    data object ResetFilters : RecipeExploreEvent
}