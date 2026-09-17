package com.example.tastify.comment.logic

import com.example.tastify.models.ProfileFrame

sealed interface CommentEvent {
    data class LoadComments(val recipeId: String) : CommentEvent
    data class UpdateDraftComment(val text: String) : CommentEvent
    data class SubmitComment(
        val authorId: String,
        val authorNickname: String,
        val authorProfileImageUri: String,
        val authorFrame: ProfileFrame,
        val authorRole: String
    ) : CommentEvent
    data class DeleteComment(val commentId: String) : CommentEvent
    data class ToggleLike(val commentId: String, val userId: String) : CommentEvent
    data object ClearError : CommentEvent
    data class SetReplyTo(val parentCommentId: String?) : CommentEvent
}