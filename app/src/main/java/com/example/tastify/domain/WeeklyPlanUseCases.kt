package com.example.tastify.domain

import com.example.tastify.data.WeeklyPlanRepository
import com.example.tastify.models.PlannedMeal
import com.example.tastify.models.UserWeeklyPlan
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WeeklyPlanUseCases @Inject constructor(
    private val repository: WeeklyPlanRepository
) {
    fun getUserPlan(userId: String): Flow<UserWeeklyPlan?> {
        return repository.getUserPlan(userId)
    }

    suspend fun addMealToPlan(userId: String, meal: PlannedMeal) {
        repository.addMealToPlan(userId, meal)
    }

    suspend fun removeMealFromPlan(userId: String, date: String, mealType: String, recipeId: String) {
        repository.removeMealFromPlan(userId, date, mealType, recipeId)
    }
}
