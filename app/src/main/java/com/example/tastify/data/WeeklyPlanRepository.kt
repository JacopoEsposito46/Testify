package com.example.tastify.data

import com.example.tastify.models.PlannedMeal
import com.example.tastify.models.UserWeeklyPlan
import kotlinx.coroutines.flow.Flow

interface WeeklyPlanRepository {
    fun getUserPlan(userId: String): Flow<UserWeeklyPlan?>
    suspend fun addMealToPlan(userId: String, meal: PlannedMeal)
    suspend fun removeMealFromPlan(userId: String, date: String, mealType: String, recipeId: String)
}
