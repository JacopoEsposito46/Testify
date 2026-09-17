package com.example.tastify.data

import com.example.tastify.models.CompleteProfileAnalytics
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    fun getProfileAnalytics(userId: String): Flow<CompleteProfileAnalytics>
    suspend fun trackRecipeView(userId: String, recipeId: String)
    suspend fun trackRecipeLike(userId: String, recipeId: String, isLike: Boolean)
    suspend fun trackRecipeComment(userId: String, recipeId: String)
}