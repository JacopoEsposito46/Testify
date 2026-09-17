package com.example.tastify.profile.settings.logic

sealed class SettingsEvent {
    object LogoutRequested : SettingsEvent()
    data class ToggleNotifications(val enabled: Boolean) : SettingsEvent()
    data class ToggleDarkMode(val enabled: Boolean) : SettingsEvent()
}