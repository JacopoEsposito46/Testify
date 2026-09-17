package com.example.tastify.domain

import com.example.tastify.data.RecipeAnalyticsRepository
import com.example.tastify.models.RecipeDailyAnalytics
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecipeDailyAnalyticsUseCase @Inject constructor(
    private val repository: RecipeAnalyticsRepository
) {
    operator fun invoke(recipeId: String, limit: Int = 31): Flow<List<RecipeDailyAnalytics>> {
        return repository.getDailyAnalytics(recipeId, limit)
    }
}
