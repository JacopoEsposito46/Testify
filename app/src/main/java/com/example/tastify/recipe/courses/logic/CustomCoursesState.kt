package com.example.tastify.recipe.courses.logic

import com.example.tastify.models.CustomCourse
import com.example.tastify.models.Recipe

data class CustomCoursesState(
    val isLoading: Boolean = true,
    val courses: List<CustomCourse> = emptyList(),
    val favoriteCount: Int = 0,
    val favouriteRecipes: List<Recipe> = emptyList(),
    val savedRecipes: List<Recipe> = emptyList(),
    val favoriteRecipeIds: List<String> = emptyList(),
    val importedRecipes: List<Recipe> = emptyList(),
    val importedCount: Int = 0,
    val myRecipes: List<Recipe> = emptyList(),
    val errorMessage: String? = null
)