package com.example.tastify.profile.settings.logic

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastify.domain.NotificationUseCases
import com.example.tastify.notifications.NotificationWorkScheduler
import com.example.tastify.notifications.SnackbarManager
import com.example.tastify.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val notificationUseCases: NotificationUseCases,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    var state by mutableStateOf(SettingsState())
        private set

    private val currentUserId = SessionManager.CURRENT_LOGGED_IN_USER_ID ?: ""

    init {
        observeNotificationsEnabled()
        observeDarkMode()
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.LogoutRequested -> logout()
            is SettingsEvent.ToggleNotifications -> setNotificationsEnabled(event.enabled)
            is SettingsEvent.ToggleDarkMode -> {
                viewModelScope.launch {
                    SessionManager.setDarkMode(event.enabled)
                }
            }
        }
    }

    private fun observeDarkMode() {
        viewModelScope.launch {
            SessionManager.isDarkMode.collect { isDark ->
                if (isDark != null) {
                    state = state.copy(darkModeEnabled = isDark)
                } else {
                    val isSystemDark = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
                    state = state.copy(darkModeEnabled = isSystemDark)
                }
            }
        }
    }

    private fun observeNotificationsEnabled() {
        viewModelScope.launch {
            notificationUseCases.observeNotificationsEnabled(currentUserId)
                .catch { exception ->
                    state = state.copy(error = exception.message)
                    snackbarManager.showMessage("Unable to load notification settings")
                }
                .collect { enabled ->
                    state = state.copy(notificationsEnabled = enabled)
                    NotificationWorkScheduler.setNotificationsEnabled(
                        context = context,
                        userId = currentUserId,
                        enabled = enabled
                    )
                    if (enabled) {
                        NotificationWorkScheduler.schedule(context)
                    } else {
                        NotificationWorkScheduler.cancel(context)
                    }
                }
        }
    }

    private fun setNotificationsEnabled(enabled: Boolean) {
        val previousValue = state.notificationsEnabled
        state = state.copy(notificationsEnabled = enabled, error = null)
        NotificationWorkScheduler.setNotificationsEnabled(context, currentUserId, enabled)

        viewModelScope.launch {
            runCatching {
                notificationUseCases.setNotificationsEnabled(currentUserId, enabled)
            }.onFailure { exception ->
                state = state.copy(
                    notificationsEnabled = previousValue,
                    error = exception.message
                )
                NotificationWorkScheduler.setNotificationsEnabled(
                    context,
                    currentUserId,
                    previousValue
                )
                snackbarManager.showMessage("Unable to update notification settings")
            }.onSuccess {
                if (enabled) {
                    NotificationWorkScheduler.schedule(context)
                } else {
                    NotificationWorkScheduler.cancel(context)
                }
            }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)
            val result = SessionManager.signOut()

            result.fold(
                onSuccess = {
                    state = state.copy(isLoading = false, logoutSuccess = true)
                },
                onFailure = { exception ->
                    state = state.copy(isLoading = false, error = exception.message)
                }
            )
        }
    }
}
