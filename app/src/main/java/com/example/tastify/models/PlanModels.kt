package com.example.tastify.plan.models

data class DayDate(
    val dayName: String,
    val dayNumber: String,
    val isSelected: Boolean
)

data class PlannedRecipe(
    val id: String,
    val title: String,
    val imageUrl: String,
    val rating: Double,
    val timeMinutes: Int,
    val calories: Int,
    val ingredientsSummary: String
)

data class MealSlot(
    val type: String,
    val time: String,
    val totalCalories: Int,
    val recipes: List<PlannedRecipe>
)