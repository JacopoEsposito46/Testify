package com.example.tastify.notifications.logic

import com.example.tastify.models.Notification

data class NotificationState(
    val notifications: List<Notification> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val snackbarMessage: String? = null,
    val searchQuery: String = "",
    val selectedIds: Set<String> = emptySet(),
    val isSelectionMode: Boolean = false
)
