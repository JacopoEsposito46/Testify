package com.example.tastify.recipe.weekly.logic

import com.example.tastify.plan.models.DayDate
import com.example.tastify.plan.models.MealSlot

data class WeeklyPlanningUiState(
    val days: List<DayDate> = emptyList(),
    val mealSlots: List<MealSlot> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    
    val isBottomSheetOpen: Boolean = false,
    val selectedMealTypeForAdd: String? = null,
    val selectedDateForAdd: String? = null,
    
    val customCourses: List<com.example.tastify.models.CustomCourse> = emptyList(),
    val favoriteRecipes: List<com.example.tastify.models.Recipe> = emptyList(),
    val importedRecipes: List<com.example.tastify.models.Recipe> = emptyList(),
    val myRecipes: List<com.example.tastify.models.Recipe> = emptyList(),
    val recipesById: Map<String, com.example.tastify.models.Recipe> = emptyMap(),
    
    val displayedRecipes: List<com.example.tastify.models.Recipe> = emptyList(),
    val selectedFilter: String = "All"
)
