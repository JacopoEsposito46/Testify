package com.example.tastify.comment.logic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.tastify.domain.AddCommentUseCase
import com.example.tastify.domain.DeleteCommentUseCase
import com.example.tastify.domain.GetCommentsUseCase
import com.example.tastify.domain.GetRecipeByIdUseCase
import com.example.tastify.domain.NotificationUseCases
import com.example.tastify.domain.ToggleLikeCommentUseCase
import com.example.tastify.domain.TrackRecipeCommentUseCase
import com.example.tastify.models.Comment
import com.example.tastify.models.ProfileFrame
import com.example.tastify.models.Recipe
import com.example.tastify.navigation.CommentsRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val toggleLikeCommentUseCase: ToggleLikeCommentUseCase,
    private val getRecipeByIdUseCase: GetRecipeByIdUseCase,
    private val trackRecipeCommentUseCase: TrackRecipeCommentUseCase,
    private val notificationUseCases: NotificationUseCases
) : ViewModel() {

    private val route = savedStateHandle.toRoute<CommentsRoute>()
    private val recipeId = route.recipeId

    private val _state = MutableStateFlow(CommentState(recipeId = recipeId))
    val state: StateFlow<CommentState> = _state.asStateFlow()

    private var commentsJob: Job? = null
    private var isSubmittingComment = false

    init {
        observeComments()
    }

    fun onEvent(event: CommentEvent) {
        when(event) {
            is CommentEvent.LoadComments -> observeComments()
            is CommentEvent.UpdateDraftComment -> _state.update { it.copy(draftCommentText = event.text, commentError = null) }
            is CommentEvent.SubmitComment -> submitComment(
                event.authorId,
                event.authorNickname,
                event.authorProfileImageUri,
                event.authorFrame,
                event.authorRole
            )
            is CommentEvent.DeleteComment -> deleteComment(event.commentId)
            is CommentEvent.ToggleLike -> toggleLike(event.commentId, event.userId)
            is CommentEvent.ClearError -> _state.update { it.copy(commentError = null) }
            is CommentEvent.SetReplyTo -> _state.update { it.copy(replyingToCommentId = event.parentCommentId) }
        }
    }

    private fun observeComments() {
        _state.update { it.copy(isLoading = true) }
        commentsJob?.cancel()
        commentsJob = viewModelScope.launch {
            getCommentsUseCase(recipeId).collect { rawComments ->
                val comments = rawComments
                    .filter { it.parentCommentId == null }
                    .sortedByDescending { it.timestamp }
                    .map { main ->
                        CommentAndRepliesState(
                            mainComment = main,
                            replies = rawComments
                                .filter { it.parentCommentId == main.commentId }
                                .sortedBy { it.timestamp }
                        )
                    }
                _state.update { it.copy(commentThreads = comments, isLoading = false) }
            }
        }
    }

    private fun submitComment(
        authorId: String,
        authorNickname: String,
        authorProfileImageUri: String,
        authorFrame: ProfileFrame,
        authorRole: String
    ) {
        val currentState = _state.value
        if (currentState.recipeId.isBlank() || isSubmittingComment) return

        if (validateComment(currentState.draftCommentText)) {
            isSubmittingComment = true
            viewModelScope.launch {
                try {
                    val comment = Comment(
                        recipeId = currentState.recipeId,
                        parentCommentId = currentState.replyingToCommentId,
                        authorId = authorId,
                        authorNickname = authorNickname,
                        authorProfileImageUri = authorProfileImageUri,
                        authorFrame = authorFrame,
                        authorRole = authorRole,
                        text = currentState.draftCommentText.trim()
                    )
                    val recipe = getRecipeByIdUseCase(currentState.recipeId).first()
                    val parentComment = currentState.replyingToCommentId
                        ?.let { parentId -> currentState.findComment(parentId) }
                    addCommentUseCase(comment)
                    notifyCommentIfNeeded(
                        comment = comment,
                        recipe = recipe,
                        parentComment = parentComment
                    )
                    _state.update { it.copy(draftCommentText = "", replyingToCommentId = null, commentError = null) }
                } finally {
                    isSubmittingComment = false
                }
            }
        }
    }

    private fun deleteComment(commentId: String) {
        if (recipeId.isNotBlank()) {
            viewModelScope.launch { deleteCommentUseCase(recipeId, commentId) }
        }
    }

    private fun toggleLike(commentId: String, userId: String) {
        if (recipeId.isNotBlank()) {
            viewModelScope.launch { toggleLikeCommentUseCase(recipeId, commentId, userId) }
        }
    }

    private fun validateComment(text: String): Boolean {
        var isValid = true
        var err: String? = null
        if (text.trim().isEmpty()) {
            err = "The comment cannot be empty."
            isValid = false
        } else if (text.length > 500) {
            err = "The comment cannot exceed 500 characters."
            isValid = false
        }
        _state.update { it.copy(commentError = err) }
        return isValid
    }

    private suspend fun notifyCommentIfNeeded(
        comment: Comment,
        recipe: Recipe?,
        parentComment: Comment?
    ) {
        if (recipe == null) return

        val recipientId = when {
            comment.authorId != recipe.authorId -> recipe.authorId
            parentComment != null && parentComment.authorId != recipe.authorId -> parentComment.authorId
            else -> null
        }

        if (recipientId.isNullOrBlank() || recipientId == comment.authorId) return

        notificationUseCases.notifyComment(
            recipientId = recipientId,
            commenterName = comment.authorNickname,
            recipeTitle = recipe.title,
            recipeId = recipe.recipeId,
            isOwnerReply = comment.authorId == recipe.authorId && parentComment != null
        )
    }

    private fun CommentState.findComment(commentId: String): Comment? {
        return commentThreads.firstNotNullOfOrNull { thread ->
            if (thread.mainComment.commentId == commentId) {
                thread.mainComment
            } else {
                thread.replies.firstOrNull { it.commentId == commentId }
            }
        }
    }
}