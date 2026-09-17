package com.example.tastify.models

import java.util.UUID

data class Review(
    val reviewId: String = UUID.randomUUID().toString(),
    val recipeId: String = "",
    val authorId: String = "",
    val authorNickname: String = "",
    val authorProfileImageUri: String = "",
    val authorFrame: ProfileFrame = ProfileFrame.BRONZE,
    val authorRole: String = "",
    val rating: Int = 0,
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val imageUris: List<String> = emptyList(),
    val authorReplyText: String? = null
)