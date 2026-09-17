package com.example.tastify.recipe.creation.logic

import com.example.tastify.models.Ingredient
import com.example.tastify.models.Recipe

data class RecipeCreationState(
    val draftRecipe: Recipe = Recipe(),
    val ingredientCatalog: List<Ingredient> = emptyList(),
    val originalRecipeForCopy: Recipe? = null,
    val copyError: String? = null,
    val coverImageError: String? = null,
    val titleError: String? = null,
    val descriptionError: String? = null,
    val timeError: String? = null,
    val caloriesError: String? = null,
    val servesError: String? = null,
    val ingredientsError: String? = null,
    val ingredientNameErrors: Map<Int, String> = emptyMap(),
    val ingredientQuantityErrors: Map<Int, String> = emptyMap(),
    val ingredientUnitErrors: Map<Int, String> = emptyMap(),
    val stepsError: String? = null,
    val stepImageErrors: Map<Int, String> = emptyMap(),
    val stepTitleErrors: Map<Int, String> = emptyMap(),
    val stepDescriptionErrors: Map<Int, String> = emptyMap(),
    
    val isLoading: Boolean = false,
    val saveSuccess: Boolean = false
)
