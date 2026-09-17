package com.example.tastify.domain

import com.example.tastify.data.ReviewRepository
import com.example.tastify.models.Review
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReviewsUseCase @Inject constructor(private val repository: ReviewRepository) {
    operator fun invoke(recipeId: String): Flow<List<Review>> = repository.getReviewsForRecipe(recipeId)
}

class AddReviewUseCase @Inject constructor(private val repository: ReviewRepository) {
    suspend operator fun invoke(review: Review) = repository.addReview(review)
}

class DeleteReviewUseCase @Inject constructor(private val repository: ReviewRepository) {
    suspend operator fun invoke(recipeId: String, reviewId: String) = repository.deleteReview(recipeId, reviewId)
}

class AddReplyToReviewUseCase @Inject constructor(private val repository: ReviewRepository) {
    suspend operator fun invoke(recipeId: String, reviewId: String, replyText: String?) = repository.addReplyToReview(recipeId, reviewId, replyText)
}