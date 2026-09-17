package com.example.tastify.notifications.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tastify.components.SharedBottomNavBar
import com.example.tastify.components.SharedTopBar
import com.example.tastify.models.Notification
import com.example.tastify.notifications.logic.NotificationEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    notifications: List<Notification>,
    searchQuery: String = "",
    isSelectionMode: Boolean = false,
    selectedIds: Set<String> = emptySet(),
    onEvent: (NotificationEvent) -> Unit,
    onNotificationClick: (Notification) -> Unit,
    onNavigateBack: () -> Unit = {},
    onNavigateToRecipeProposalList: () -> Unit = {},
    onNavigateToMyProfile: () -> Unit = {},
    onNavigateToCookBook: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    unreadNotificationCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            if (isSelectionMode) {
                // Contextual action bar for multi-selection
                TopAppBar(
                    title = { Text("${selectedIds.size} selected") },
                    navigationIcon = {
                        IconButton(onClick = { onEvent(NotificationEvent.ClearSelection) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Exit selection"
                            )
                        }
                    },
                    actions = {
                        // Select All / Deselect All toggle
                        val allSelected = notifications.isNotEmpty() &&
                                selectedIds.size == notifications.size
                        IconButton(
                            onClick = {
                                if (allSelected) {
                                    onEvent(NotificationEvent.ClearSelection)
                                } else {
                                    onEvent(NotificationEvent.SelectAll)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.SelectAll,
                                contentDescription = if (allSelected) "Deselect all" else "Select all",
                                tint = if (allSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onEvent(NotificationEvent.MarkSelectedAsRead) }) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Mark selected as read",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { onEvent(NotificationEvent.DeleteSelected) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete selected",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                )
            } else {
                SharedTopBar(
                    title = "Notifications",
                    onBack = onNavigateBack,
                    actions = {
                        IconButton(onClick = { onEvent(NotificationEvent.MarkAllAsRead) }) {
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Mark all as read",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            SharedBottomNavBar(
                selectedRoute = "updates",
                onRouteSelected = { route ->
                    when (route) {
                        "home" -> onNavigateToRecipeProposalList()
                        "cookbook" -> onNavigateToCookBook()
                        "profile" -> onNavigateToMyProfile()
                        "chat" -> onNavigateToChat()
                    }
                },
                unreadNotificationCount = unreadNotificationCount
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { onEvent(NotificationEvent.SearchQueryChanged(it)) },
                placeholder = { Text("Search notifications...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "Search"
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onEvent(NotificationEvent.SearchQueryChanged("")) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = "Clear search"
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            if (notifications.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No results for \"$searchQuery\"" else "No notifications",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = notifications,
                        key = { it.id }
                    ) { notification ->
                        NotificationItem(
                            notification = notification,
                            isSelectionMode = isSelectionMode,
                            isSelected = notification.id in selectedIds,
                            onClick = {
                                if (isSelectionMode) {
                                    onEvent(NotificationEvent.ToggleSelection(notification.id))
                                } else {
                                    onEvent(NotificationEvent.MarkAsRead(notification.id))
                                    onNotificationClick(notification)
                                }
                            },
                            onDelete = {
                                onEvent(NotificationEvent.DeleteNotification(notification.id))
                            },
                            onMarkAsRead = {
                                onEvent(NotificationEvent.MarkAsRead(notification.id))
                            },
                            onLongPress = {
                                if (!isSelectionMode) {
                                    onEvent(NotificationEvent.LongPressNotification(notification.id))
                                }
                            },
                            onToggleSelection = {
                                onEvent(NotificationEvent.ToggleSelection(notification.id))
                            }
                        )
                    }
                }
            }
        }
    }
}