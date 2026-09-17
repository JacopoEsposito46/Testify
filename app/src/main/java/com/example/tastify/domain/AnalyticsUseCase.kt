package com.example.tastify.domain

import com.example.tastify.data.AnalyticsRepository
import com.example.tastify.models.CompleteProfileAnalytics
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProfileAnalyticsUseCase @Inject constructor(
    private val repository: AnalyticsRepository
) {
    operator fun invoke(userId: String): Flow<CompleteProfileAnalytics> {
        return repository.getProfileAnalytics(userId)
    }
}

class TrackRecipeViewUseCase @Inject constructor(
    private val repository: AnalyticsRepository
) {
    suspend operator fun invoke(userId: String, recipeId: String) {
        repository.trackRecipeView(userId, recipeId)
    }
}

class TrackRecipeLikeUseCase @Inject constructor(
    private val repository: AnalyticsRepository
) {
    suspend operator fun invoke(userId: String, recipeId: String, isLike: Boolean) {
        repository.trackRecipeLike(userId, recipeId, isLike)
    }
}

class TrackRecipeCommentUseCase @Inject constructor(
    private val repository: AnalyticsRepository
) {
    suspend operator fun invoke(userId: String, recipeId: String) {
        repository.trackRecipeComment(userId, recipeId)
    }
}