package com.example.tastify.authentication.logic

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastify.data.ProfileRepository
import com.example.tastify.models.ImageSource
import com.example.tastify.models.UserProfile
import com.example.tastify.notifications.NotificationWorkScheduler
import com.example.tastify.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    var uiState by mutableStateOf(AuthenticationState())
        private set

    fun onEvent(event: AuthenticationEvent) {
        when (event) {
            is AuthenticationEvent.OnAuthenticationClick -> {
                signInWithGoogle(event.context)
            }
            is AuthenticationEvent.OnNicknameChange -> {
                uiState = uiState.copy(nickname = event.nickname)
            }
            is AuthenticationEvent.OnCuisineToggle -> {
                val currentList = uiState.selectedCuisine.toMutableList()
                if (currentList.contains(event.cuisine)) {
                    currentList.remove(event.cuisine)
                } else {
                    currentList.add(event.cuisine)
                }
                uiState = uiState.copy(selectedCuisine = currentList)
            }
            is AuthenticationEvent.OnDietaryToggle -> {
                val currentList = uiState.selectedDietary.toMutableList()
                if (currentList.contains(event.dietary)) {
                    currentList.remove(event.dietary)
                } else {
                    currentList.add(event.dietary)
                }
                uiState = uiState.copy(selectedDietary = currentList)
            }
            is AuthenticationEvent.OnRoleToggle -> {
                uiState = uiState.copy(selectedRole = event.role)
            }
            is AuthenticationEvent.ClearNicknameError -> { uiState = uiState.copy(nicknameError = null) }
            is AuthenticationEvent.OnSubmitRegistration -> {
                completeRegistration()
            }
        }
    }

    private fun validateNickname(): Boolean {
        var isValid = true
        var nickErr: String? = null

        if (uiState.nickname.isBlank()) { nickErr = "The nickname cannot be empty."; isValid = false }

        uiState = uiState.copy(nicknameError = nickErr)

        return isValid
    }

    private fun signInWithGoogle(context: android.content.Context) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            val result = SessionManager.signInWithGoogle(context)

            result.onSuccess { user ->
                if(user.username.isNotEmpty()) {
                    viewModelScope.launch {
                        SessionManager.setLoggedIn(true)
                        updateNotificationWork(user.notificationsEnabled)
                        uiState = uiState.copy(isAuthSuccessful = true)
                    }
                } else {
                    val extractedUrl = (user.profilePicture as? ImageSource.Uri)?.uriString ?: ""

                    uiState = uiState.copy(
                        isLoading = false,
                        showRegistrationForm = true,
                        name = user.name,
                        surname = user.surname,
                        email = user.email,
                        photoUrl = extractedUrl
                    )
                }
            }.onFailure { exception ->
                uiState = uiState.copy(
                    isLoading = false,
                    error = exception.message ?: "Login error"
                )
            }
        }
    }

    private fun completeRegistration() {

        if (!validateNickname()) {
            return
        }

        val uid = SessionManager.CURRENT_LOGGED_IN_USER_ID

        if (uid == null) {
            uiState = uiState.copy(error = "User not authenticated")
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            val updatedProfile = UserProfile(
                internalID = uid,
                username = uiState.nickname,
                name = uiState.name,
                surname = uiState.surname,
                email = uiState.email,
                profilePictureUrl = uiState.photoUrl,
                profilePicture = ImageSource.Uri(uiState.photoUrl),
                cookingRole = uiState.selectedRole,
                selectedDietaryRestriction = uiState.selectedDietary,
                favoritesCuisineType = uiState.selectedCuisine
            )

            try {
                profileRepository.updateUserProfile(updatedProfile)
                SessionManager.setLoggedIn(true)
                updateNotificationWork(updatedProfile.notificationsEnabled)

                uiState = uiState.copy(
                    isLoading = false,
                    showRegistrationForm = false,
                    isAuthSuccessful = true
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Error during registration: ${e.message}"
                )
            }
        }
    }

    private fun updateNotificationWork(enabled: Boolean) {
        val userId = SessionManager.CURRENT_LOGGED_IN_USER_ID ?: return
        NotificationWorkScheduler.setNotificationsEnabled(context, userId, enabled)
        if (enabled) {
            NotificationWorkScheduler.schedule(context)
        } else {
            NotificationWorkScheduler.cancel(context)
        }
    }
}
