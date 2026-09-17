package com.example.tastify.data

import com.example.tastify.models.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotificationsForUser(userId: String): Flow<List<Notification>>
    fun getUnreadCount(userId: String): Flow<Int>
    fun observeNotificationsEnabled(userId: String): Flow<Boolean>
    suspend fun setNotificationsEnabled(userId: String, enabled: Boolean)
    suspend fun markAsRead(userId: String, notificationId: String)
    suspend fun markAllAsRead(userId: String)
    suspend fun createNotification(notification: Notification)
    suspend fun deleteNotification(userId: String, notificationId: String)
}