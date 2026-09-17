package com.example.tastify.profile.settings.logic

data class SettingsState(
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val logoutSuccess: Boolean = false,
    val error: String? = null
)