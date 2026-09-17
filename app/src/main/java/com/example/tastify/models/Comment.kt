package com.example.tastify.models

import java.util.UUID

data class Comment(
    val commentId: String = UUID.randomUUID().toString() ,
    val recipeId: String = "",
    val authorId: String = "",
    val parentCommentId: String? = null,
    val authorNickname: String = "",
    val authorProfileImageUri: String = "",
    val authorFrame: ProfileFrame = ProfileFrame.BRONZE,
    val authorRole: String = "",
    val text: String = "",
    val likedBy: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
)