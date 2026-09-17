package com.example.tastify.review.logic

import com.example.tastify.models.ProfileFrame

sealed interface ReviewEvent {
    data class LoadReviews(val recipeId: String) : ReviewEvent
    data class UpdateDraftReview(val rating: Int, val text: String) : ReviewEvent
    data class AddDraftReviewImage(val uri: String) : ReviewEvent
    data class RemoveDraftReviewImage(val uri: String) : ReviewEvent
    data class SubmitReview(
        val authorId: String,
        val authorNickname: String,
        val authorProfileImageUri: String,
        val authorFrame: ProfileFrame,
        val authorRole: String
    ) : ReviewEvent
    data class SubmitAuthorReply(val reviewId: String, val replyText: String?) : ReviewEvent
    data class DeleteReview(val reviewId: String) : ReviewEvent
    data object ClearError : ReviewEvent
}