package com.example.tastify.notifications.logic

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastify.domain.NotificationUseCases
import com.example.tastify.models.Notification
import com.example.tastify.notifications.NotificationWorkScheduler
import com.example.tastify.notifications.SnackbarManager
import com.example.tastify.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class NotificationViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val notificationUseCases: NotificationUseCases,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val currentUserId = SessionManager.CURRENT_LOGGED_IN_USER_ID ?: ""

    private val _state = MutableStateFlow(NotificationState(isLoading = true))
    val state: StateFlow<NotificationState> = _state.asStateFlow()

    // Track previous unread count to detect new notifications
    private var previousUnreadCount = -1

    // Entry point events
    fun onEvent(event: NotificationEvent) {
        when (event) {
            is NotificationEvent.MarkAsRead -> markAsRead(event.notificationId)
            is NotificationEvent.MarkAllAsRead -> markAllAsRead()
            is NotificationEvent.NotificationClicked -> onNotificationClicked(event.notification)
            is NotificationEvent.DeleteNotification -> deleteNotification(event.notificationId)
            is NotificationEvent.SearchQueryChanged -> onSearchQueryChanged(event.query)
            is NotificationEvent.SnackbarConsumed -> consumeSnackbar()
            is NotificationEvent.LongPressNotification -> enterSelectionMode(event.notificationId)
            is NotificationEvent.ToggleSelection -> toggleSelection(event.notificationId)
            is NotificationEvent.ClearSelection -> clearSelection()
            is NotificationEvent.SelectAll -> selectAll()
            is NotificationEvent.MarkSelectedAsRead -> markSelectedAsRead()
            is NotificationEvent.DeleteSelected -> deleteSelected()
        }
    }

    // All notifications for internal use
    private var allNotifications: List<Notification> = emptyList()

    // When true, snackbar is suppressed
    private var snackbarSuppressed = false
    private var notificationsScreenVisible = false

    fun suppressSnackbar(suppress: Boolean) {
        snackbarSuppressed = suppress
    }

    fun setNotificationsScreenVisible(visible: Boolean) {
        notificationsScreenVisible = visible
        if (visible) {
            updatePollingCursor(allNotifications)
        }
    }

    private val _searchQuery = MutableStateFlow("")

    init {
        observeNotifications()
        observeUnreadCount()
        observeSearchQuery()
    }

    private fun observeSearchQuery() {
        _searchQuery
            .debounce { if (it.isEmpty()) 0L else 300L }
            .onEach { query ->
                _state.update {
                    it.copy(notifications = filterNotifications(allNotifications, query))
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeNotifications() {
        notificationUseCases.getNotifications(currentUserId)
            .onEach { notifications ->
                // Detect new unread notifications by comparing with previous list
                if (allNotifications.isNotEmpty()) {
                    val previousIds = allNotifications.map { it.id }.toSet()
                    val newUnread = notifications.firstOrNull { it.id !in previousIds && !it.read }
                    if (newUnread != null && !snackbarSuppressed) {
                        snackbarManager.showMessage(
                            text = newUnread.title,
                            actionLabel = "View"
                        )
                    }
                }

                allNotifications = notifications
                if (notificationsScreenVisible) {
                    updatePollingCursor(notifications)
                }
                _state.update {
                    it.copy(
                        notifications = filterNotifications(notifications, it.searchQuery),
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun updatePollingCursor(notifications: List<Notification>) {
        val newestTimestamp = notifications.maxOfOrNull { it.timestamp } ?: System.currentTimeMillis()
        NotificationWorkScheduler.markNotificationsViewed(
            context = context,
            userId = currentUserId,
            timestamp = newestTimestamp
        )
    }

    private fun observeUnreadCount() {
        notificationUseCases.getUnreadCount(currentUserId)
            .onEach { count ->
                previousUnreadCount = count
                _state.update { it.copy(unreadCount = count) }
            }
            .launchIn(viewModelScope)
    }

    private fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationUseCases.markAsRead(currentUserId, notificationId)
        }
    }

    private fun markAllAsRead() {
        viewModelScope.launch {
            notificationUseCases.markAllAsRead(currentUserId)
        }
    }

    private fun onNotificationClicked(notification: Notification) {
        viewModelScope.launch {
            if (!notification.read) {
                notificationUseCases.markAsRead(currentUserId, notification.id)
            }
        }
    }

    private fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationUseCases.deleteNotification(currentUserId, notificationId)
        }
    }

    private fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        _searchQuery.value = query
    }

    private fun filterNotifications(notifications: List<Notification>, query: String): List<Notification> {
        if (query.isBlank()) return notifications
        return notifications.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.message.contains(query, ignoreCase = true)
        }
    }

    private fun consumeSnackbar() {
        _state.update { it.copy(snackbarMessage = null) }
    }

    fun showSnackbar(message: String) {
        _state.update { it.copy(snackbarMessage = message) }
    }

    // --- Multi-selection logic ---

    private fun enterSelectionMode(notificationId: String) {
        _state.update {
            it.copy(
                isSelectionMode = true,
                selectedIds = setOf(notificationId)
            )
        }
    }

    private fun toggleSelection(notificationId: String) {
        _state.update { current ->
            val newSelection = if (notificationId in current.selectedIds) {
                current.selectedIds - notificationId
            } else {
                current.selectedIds + notificationId
            }
            // Exit selection mode automatically if all deselected
            if (newSelection.isEmpty()) {
                current.copy(isSelectionMode = false, selectedIds = emptySet())
            } else {
                current.copy(selectedIds = newSelection)
            }
        }
    }

    private fun clearSelection() {
        _state.update { it.copy(isSelectionMode = false, selectedIds = emptySet()) }
    }

    private fun selectAll() {
        _state.update { current ->
            val allIds = current.notifications.map { it.id }.toSet()
            current.copy(selectedIds = allIds)
        }
    }

    private fun markSelectedAsRead() {
        viewModelScope.launch {
            val ids = _state.value.selectedIds.toList()
            ids.forEach { notificationUseCases.markAsRead(currentUserId, it) }
            clearSelection()
        }
    }

    private fun deleteSelected() {
        viewModelScope.launch {
            val ids = _state.value.selectedIds.toList()
            ids.forEach { notificationUseCases.deleteNotification(currentUserId, it) }
            clearSelection()
        }
    }
}

