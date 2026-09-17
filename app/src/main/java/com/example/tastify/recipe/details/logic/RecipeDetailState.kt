package com.example.tastify.recipe.details.logic

import com.example.tastify.models.CustomCourse
import com.example.tastify.models.Recipe

data class RecipeDetailState(
    val selectedRecipe: Recipe? = null,
    val isOwner: Boolean = false,
    val isLoadingDetails: Boolean = true,
    val courses: List<CustomCourse> = emptyList(),
    val isFavorite: Boolean = false,
    val isDone: Boolean = false,
)
