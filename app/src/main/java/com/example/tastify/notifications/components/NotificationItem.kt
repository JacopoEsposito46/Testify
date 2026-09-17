package com.example.tastify.notifications.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Recommend
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.tastify.models.Notification
import com.example.tastify.models.NotificationType
import com.example.tastify.ui.theme.NotificationColors
import com.example.tastify.utils.toNotificationRelativeTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMarkAsRead: () -> Unit = {},
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongPress: () -> Unit = {},
    onToggleSelection: () -> Unit = {}
) {
    // In selection mode, disable swipe-to-dismiss
    if (isSelectionMode) {
        NotificationItemContent(
            notification = notification,
            isSelectionMode = true,
            isSelected = isSelected,
            onClick = onToggleSelection,
            onLongPress = onLongPress,
            modifier = modifier
        )
    } else {
        val dismissState = rememberSwipeToDismissBoxState()

        LaunchedEffect(dismissState.currentValue) {
            when (dismissState.currentValue) {
                SwipeToDismissBoxValue.EndToStart -> onDelete()
                SwipeToDismissBoxValue.StartToEnd -> {
                    onMarkAsRead()
                    dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                }
                SwipeToDismissBoxValue.Settled -> { /* no-op */ }
            }
        }

        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val direction = dismissState.dismissDirection
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when (direction) {
                                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                                SwipeToDismissBoxValue.StartToEnd -> NotificationColors.MarkAsRead
                                else -> Color.Transparent
                            }
                        ),
                    contentAlignment = when (direction) {
                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                        else -> Alignment.CenterStart
                    }
                ) {
                    when (direction) {
                        SwipeToDismissBoxValue.EndToStart -> Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.padding(end = 20.dp)
                        )
                        SwipeToDismissBoxValue.StartToEnd -> Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Mark as read",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(start = 20.dp)
                        )
                        else -> {}
                    }
                }
            },
            enableDismissFromStartToEnd = !notification.read,
            modifier = modifier
        ) {
            NotificationItemContent(
                notification = notification,
                isSelectionMode = false,
                isSelected = false,
                onClick = onClick,
                onLongPress = onLongPress,
                modifier = Modifier
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotificationItemContent(
    notification: Notification,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongPress
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
            else if (!notification.read)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // checkbox in selection mode
            if (isSelectionMode) {
                SelectionIndicator(isSelected = isSelected)
            } else {
                NotificationTypeIcon(type = notification.type)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (!notification.read) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = notification.timestamp.toNotificationRelativeTime(),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Unread dot indicator
            if (!isSelectionMode && !notification.read) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun SelectionIndicator(isSelected: Boolean) {
    Box(
        modifier = Modifier.size(50.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (isSelected) "Selected" else "Not selected",
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun NotificationTypeIcon(type: NotificationType) {
    val icon = when (type) {
        NotificationType.DUPLICATION -> Icons.Default.ContentCopy
        NotificationType.REVIEW_RECEIVED -> Icons.Default.RateReview
        NotificationType.RECOMMENDATION -> Icons.Default.Recommend
        NotificationType.COMMENT_RECEIVED -> Icons.AutoMirrored.Filled.Comment
    }
    val tint = when (type) {
        NotificationType.DUPLICATION -> NotificationColors.Duplication
        NotificationType.REVIEW_RECEIVED -> NotificationColors.ReviewReceived
        NotificationType.RECOMMENDATION -> NotificationColors.Recommendation
        NotificationType.COMMENT_RECEIVED -> NotificationColors.CommentReceived
    }

    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(tint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = type.name,
            tint = tint,
            modifier = Modifier.size(25.dp)
        )
    }
}
