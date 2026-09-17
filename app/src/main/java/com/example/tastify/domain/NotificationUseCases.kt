package com.example.tastify.domain

import com.example.tastify.data.NotificationRepository
import com.example.tastify.models.Notification
import com.example.tastify.models.NotificationType
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject

class NotificationUseCases @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    fun getNotifications(userId: String): Flow<List<Notification>> =
        notificationRepository.getNotificationsForUser(userId)

    fun getUnreadCount(userId: String): Flow<Int> =
        notificationRepository.getUnreadCount(userId)

    fun observeNotificationsEnabled(userId: String): Flow<Boolean> =
        notificationRepository.observeNotificationsEnabled(userId)

    suspend fun setNotificationsEnabled(userId: String, enabled: Boolean) =
        notificationRepository.setNotificationsEnabled(userId, enabled)

    suspend fun markAsRead(userId: String, notificationId: String) =
        notificationRepository.markAsRead(userId, notificationId)

    suspend fun markAllAsRead(userId: String) =
        notificationRepository.markAllAsRead(userId)

    suspend fun deleteNotification(userId: String, notificationId: String) =
        notificationRepository.deleteNotification(userId, notificationId)

    suspend fun notifyDuplication(
        originalAuthorId: String,
        forkerName: String,
        recipeTitle: String,
        recipeId: String
    ) {
        notificationRepository.createNotification(
            Notification(
                id = UUID.randomUUID().toString(),
                recipientId = originalAuthorId,
                type = NotificationType.DUPLICATION,
                title = NotificationType.DUPLICATION.defaultTitle,
                message = "$forkerName created a variant of your \"$recipeTitle\"",
                relatedRecipeId = recipeId
            )
        )
    }

    suspend fun notifyNewReview(
        recipeAuthorId: String,
        reviewerName: String,
        recipeTitle: String,
        recipeId: String,
        rating: Int
    ) {
        notificationRepository.createNotification(
            Notification(
                id = UUID.randomUUID().toString(),
                recipientId = recipeAuthorId,
                type = NotificationType.REVIEW_RECEIVED,
                title = NotificationType.REVIEW_RECEIVED.defaultTitle,
                message = "$reviewerName left a ${rating}★ review on your \"$recipeTitle\"",
                relatedRecipeId = recipeId
            )
        )
    }

    suspend fun notifyRecommendation(
        userId: String,
        recipeTitle: String,
        recipeId: String
    ) {
        notificationRepository.createNotification(
            Notification(
                id = UUID.randomUUID().toString(),
                recipientId = userId,
                type = NotificationType.RECOMMENDATION,
                title = NotificationType.RECOMMENDATION.defaultTitle,
                message = "Based on your tastes: \"$recipeTitle\"",
                relatedRecipeId = recipeId
            )
        )
    }

    suspend fun notifyComment(
        recipientId: String,
        commenterName: String,
        recipeTitle: String,
        recipeId: String,
        isOwnerReply: Boolean
    ) {
        val message = if (isOwnerReply) {
            "$commenterName replied to your comment on \"$recipeTitle\""
        } else {
            "$commenterName commented on your \"$recipeTitle\""
        }

        notificationRepository.createNotification(
            Notification(
                id = UUID.randomUUID().toString(),
                recipientId = recipientId,
                type = NotificationType.COMMENT_RECEIVED,
                title = NotificationType.COMMENT_RECEIVED.defaultTitle,
                message = message,
                relatedRecipeId = recipeId
            )
        )
    }
}
