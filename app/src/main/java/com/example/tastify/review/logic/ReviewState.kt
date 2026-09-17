package com.example.tastify.review.logic

import com.example.tastify.models.Review

data class ReviewState(
    val recipeId: String = "",
    val reviews: List<Review> = emptyList(),
    val averageRating: Double = 0.0,
    val isLoading: Boolean = false,

    val draftReviewRating: Int = 0,
    val draftReviewText: String = "",
    val draftReviewImageUris: List<String> = emptyList(),
    val reviewError: String? = null,
    val submitSuccess: Boolean = false,

    val recipeAuthorProfileImageUri: String = "",
    val recipeAuthorNickname: String = ""
)