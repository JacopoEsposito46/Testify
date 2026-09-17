package com.example.tastify.data

import com.example.tastify.models.Review
import kotlinx.coroutines.flow.Flow

interface ReviewRepository {
    fun getReviewsForRecipe(recipeId: String): Flow<List<Review>>
    suspend fun addReview(review: Review)
    suspend fun deleteReview(recipeId: String, reviewId: String)
    suspend fun addReplyToReview(recipeId: String, reviewId: String, replyText: String?)
}