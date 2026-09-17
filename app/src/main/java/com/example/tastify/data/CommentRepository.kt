package com.example.tastify.data

import com.example.tastify.models.Comment
import kotlinx.coroutines.flow.Flow

interface CommentRepository {
    fun getCommentsForRecipe(recipeId: String): Flow<List<Comment>>
    suspend fun addComment(comment: Comment)
    suspend fun deleteComment(recipeId: String, commentId: String)
    suspend fun toggleLike(recipeId: String, commentId: String, userId: String)
    suspend fun unlikeComment(recipeId: String, commentId: String, userId: String)
}