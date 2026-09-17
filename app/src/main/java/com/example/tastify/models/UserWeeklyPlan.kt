package com.example.tastify.models

data class UserWeeklyPlan(
    val userId: String = "",
    val plannedMeals: List<PlannedMeal> = emptyList()
)

data class PlannedMeal(
    val date: String = "",
    val mealType: String = "",
    val recipeId: String = ""
)
