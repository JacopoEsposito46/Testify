package com.example.tastify.models

data class Notification(
    val id: String = "",
    val recipientId: String = "",   // UID Firebase (per ora mock "user_101")
    val type: NotificationType = NotificationType.RECOMMENDATION,
    val title: String = "",
    val message: String = "",
    val relatedRecipeId: String? = null,
    val read: Boolean = false,
    val systemNotificationDelivered: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
