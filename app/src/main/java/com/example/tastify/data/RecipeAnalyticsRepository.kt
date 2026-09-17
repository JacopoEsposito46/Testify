package com.example.tastify.data

import com.example.tastify.models.RecipeDailyAnalytics
import kotlinx.coroutines.flow.Flow

interface RecipeAnalyticsRepository {
    fun getDailyAnalytics(recipeId: String, limit: Int = 31): Flow<List<RecipeDailyAnalytics>>
}
