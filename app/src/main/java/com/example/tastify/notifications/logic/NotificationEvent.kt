package com.example.tastify.notifications.logic

import com.example.tastify.models.Notification

sealed class NotificationEvent {
    data class MarkAsRead(val notificationId: String) : NotificationEvent()
    data object MarkAllAsRead : NotificationEvent()
    data class NotificationClicked(val notification: Notification) : NotificationEvent()
    data class DeleteNotification(val notificationId: String) : NotificationEvent()
    data class SearchQueryChanged(val query: String) : NotificationEvent()
    data object SnackbarConsumed : NotificationEvent()

    // Multi-selection events
    data class LongPressNotification(val notificationId: String) : NotificationEvent()
    data class ToggleSelection(val notificationId: String) : NotificationEvent()
    data object ClearSelection : NotificationEvent()
    data object SelectAll : NotificationEvent()
    data object MarkSelectedAsRead : NotificationEvent()
    data object DeleteSelected : NotificationEvent()
}

