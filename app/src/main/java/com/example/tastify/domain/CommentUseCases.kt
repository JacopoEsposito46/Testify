package com.example.tastify.domain

import com.example.tastify.data.CommentRepository
import com.example.tastify.models.Comment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCommentsUseCase @Inject constructor(private val repository: CommentRepository) {
    operator fun invoke(recipeId: String): Flow<List<Comment>> = repository.getCommentsForRecipe(recipeId)
}

class AddCommentUseCase @Inject constructor(private val repository: CommentRepository) {
    suspend operator fun invoke(comment: Comment) = repository.addComment(comment)
}

class DeleteCommentUseCase @Inject constructor(private val repository: CommentRepository) {
    suspend operator fun invoke(recipeId: String, commentId: String) = repository.deleteComment(recipeId, commentId)
}

class ToggleLikeCommentUseCase @Inject constructor(private val repository: CommentRepository) {
    suspend operator fun invoke(recipeId: String, commentId: String, userId: String) = repository.toggleLike(recipeId, commentId, userId)
}

class UnlikeCommentUseCase @Inject constructor(private val repository: CommentRepository) {
    suspend operator fun invoke(recipeId: String, commentId: String, userId: String) =
        repository.unlikeComment(recipeId, commentId, userId)
}