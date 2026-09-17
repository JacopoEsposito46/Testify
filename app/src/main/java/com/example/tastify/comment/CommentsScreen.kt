package com.example.tastify.comment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tastify.comment.components.CommentInputBar
import com.example.tastify.comment.components.CommentItem
import com.example.tastify.comment.logic.CommentEvent
import com.example.tastify.comment.logic.CommentViewModel
import com.example.tastify.components.SharedTopBar
import com.example.tastify.models.ProfileFrame
import com.example.tastify.utils.SessionManager

@Composable
fun CommentsScreen(
    viewModel: CommentViewModel,
    onBack: () -> Unit,
    currentUserNickname: String = "",
    currentUserProfileImageUri: String = "",
    currentUserFrame: ProfileFrame = ProfileFrame.BRONZE,
    currentUserRole: String = ""
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val safeUserId = SessionManager.CURRENT_LOGGED_IN_USER_ID ?: ""

    Scaffold(
        topBar = {
            SharedTopBar(
                title = "Comments",
                onBack = onBack,
                actions = {}
            )
        },
        bottomBar = {
            CommentInputBar(
                text = state.draftCommentText,
                isReplying = state.replyingToCommentId != null,
                onTextChange = { viewModel.onEvent(CommentEvent.UpdateDraftComment(it)) },
                onSubmit = {
                    if (safeUserId.isNotEmpty()) {
                        viewModel.onEvent(
                            CommentEvent.SubmitComment(
                                authorId = safeUserId,
                                authorNickname = currentUserNickname,
                                authorProfileImageUri = currentUserProfileImageUri,
                                authorFrame = currentUserFrame,
                                authorRole = currentUserRole
                            )
                        )
                    }
                },
                onCancelReply = {
                    viewModel.onEvent(CommentEvent.SetReplyTo(null))
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                items(state.commentThreads) { thread ->
                    CommentItem(
                        thread = thread,
                        currentUserId = safeUserId,
                        onLikeClick = { commentId ->
                            viewModel.onEvent(CommentEvent.ToggleLike(commentId, safeUserId))
                        },
                        onReplyClick = { parentId ->
                            viewModel.onEvent(CommentEvent.SetReplyTo(parentId))
                        }
                    )
                }
            }
        }
    }
}