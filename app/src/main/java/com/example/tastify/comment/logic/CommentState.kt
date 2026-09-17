package com.example.tastify.comment.logic

import com.example.tastify.models.Comment


data class CommentAndRepliesState(
    val mainComment: Comment,
    val replies: List<Comment>
)

data class CommentState(
    val recipeId: String = "",
    val commentThreads: List<CommentAndRepliesState> = emptyList(),
    val isLoading: Boolean = false,
    val draftCommentText: String = "",
    val replyingToCommentId: String? = null,
    val commentError: String? = null
)