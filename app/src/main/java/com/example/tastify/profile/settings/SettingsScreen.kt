package com.example.tastify.profile.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tastify.components.SharedTopBar
import com.example.tastify.profile.settings.components.SettingsClickableItem
import com.example.tastify.profile.settings.components.SettingsSectionTitle
import com.example.tastify.profile.settings.components.SettingsToggleItem
import com.example.tastify.profile.settings.logic.SettingsEvent
import com.example.tastify.profile.settings.logic.SettingsState

@Composable
fun SettingsScreen(
    state: SettingsState,
    onEvent: (SettingsEvent) -> Unit,
    onBack: () -> Unit,
    onNavigateToEditProfile: () -> Unit
) {
    Scaffold(
        topBar = {
            SharedTopBar(
                title = "Settings",
                onBack = onBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                SettingsSectionTitle(title = "Account")
                SettingsClickableItem(
                    icon = Icons.Default.AccountCircle,
                    title = "Edit Profile",
                    subtitle = "Change your name, bio, and avatar",
                    onClick = onNavigateToEditProfile
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsSectionTitle(title = "Preferences")
                SettingsToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    subtitle = "Receive updates about recipes and comments",
                    checked = state.notificationsEnabled,
                    onCheckedChange = { onEvent(SettingsEvent.ToggleNotifications(it)) }
                )
                SettingsToggleItem(
                    icon = Icons.Default.Palette,
                    title = "Dark Mode",
                    subtitle = "Toggle dark theme for the app",
                    checked = state.darkModeEnabled,
                    onCheckedChange = { onEvent(SettingsEvent.ToggleDarkMode(it)) }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsClickableItem(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    title = "Logout",
                    subtitle = "Sign out of your account",
                    titleColor = MaterialTheme.colorScheme.error,
                    iconColor = MaterialTheme.colorScheme.error,
                    onClick = { onEvent(SettingsEvent.LogoutRequested) }
                )
            }
        }
    }
}