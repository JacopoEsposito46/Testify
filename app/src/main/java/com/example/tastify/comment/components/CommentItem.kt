package com.example.tastify.comment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tastify.comment.logic.CommentAndRepliesState

@Composable
fun CommentItem(
    thread: CommentAndRepliesState,
    currentUserId: String,
    onLikeClick: (String) -> Unit,
    onReplyClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CommentBody(
            comment = thread.mainComment,
            currentUserId = currentUserId,
            avatarSize = 40.dp,
            onLikeClick = { onLikeClick(thread.mainComment.commentId) },
            onReplyClick = { onReplyClick(thread.mainComment.commentId) }
        )

        if (thread.replies.isNotEmpty()) {
            thread.replies.forEach { reply ->
                Row(
                    modifier = Modifier
                        .padding(start = 20.dp, top = 16.dp)
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    CommentBody(
                        comment = reply,
                        currentUserId = currentUserId,
                        avatarSize = 32.dp,
                        onLikeClick = { onLikeClick(reply.commentId) },
                        onReplyClick = { onReplyClick(thread.mainComment.commentId) }
                    )
                }
            }
        }
    }
}