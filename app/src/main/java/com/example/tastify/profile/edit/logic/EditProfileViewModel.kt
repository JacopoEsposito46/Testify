package com.example.tastify.profile.edit.logic

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastify.domain.GetUserProfileUseCase
import com.example.tastify.domain.UploadProfilePictureUseCase
import com.example.tastify.domain.UpdateUserProfileUseCase
import com.example.tastify.models.CookingRole
import com.example.tastify.models.ImageSource
import com.example.tastify.models.ProfileFrame
import com.example.tastify.models.UserProfile
import com.example.tastify.utils.LevelSystem
import com.example.tastify.utils.SessionManager
import com.example.tastify.utils.isLocalUri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val uploadProfilePictureUseCase: UploadProfilePictureUseCase
) : ViewModel(){

    private val currentUserId = SessionManager.CURRENT_LOGGED_IN_USER_ID ?: ""

    private val _uiState = MutableStateFlow(
        EditProfileState(
            profile = UserProfile(
                internalID = currentUserId,
                username = "",
                name = "",
                surname = "",
                email = ""
            )
        )
    )
    val uiState: StateFlow<EditProfileState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getUserProfileUseCase(currentUserId).firstOrNull()?.let { currentProfile ->
                val currentLevel = LevelSystem.calculateLevel(currentProfile.experiencePoints)
                val roles = CookingRole.getAvailableRoles(currentLevel)
                val frames = getAvailableFrames(currentLevel)
                val safeProfile = currentProfile.copy(
                    chosenFrame = currentProfile.chosenFrame.takeIf { it in frames }
                        ?: LevelSystem.getFrameForLevel(currentLevel)
                )

                _uiState.update {
                    it.copy(
                        profile = safeProfile,
                        isLoading = false,
                        availableRoles = roles,
                        availableFrames = frames
                    )
                }
            } ?: run {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onEvent(event: EditProfileEvent) {
        when (event) {
            is EditProfileEvent.UpdateProfile -> {
                val currentLevel = LevelSystem.calculateLevel(event.updatedProfile.experiencePoints)
                val roles = CookingRole.getAvailableRoles(currentLevel)
                val frames = getAvailableFrames(currentLevel)
                val safeProfile = event.updatedProfile.copy(
                    chosenFrame = event.updatedProfile.chosenFrame.takeIf { it in frames }
                        ?: LevelSystem.getFrameForLevel(currentLevel)
                )
                _uiState.update {
                    it.copy(
                        profile = safeProfile,
                        availableRoles = roles,
                        availableFrames = frames
                    )
                }
            }
            is EditProfileEvent.UpdateProfilePicture -> {
                val currentProfile = _uiState.value.profile
                _uiState.update {
                    it.copy(
                        profile = currentProfile.copy(profilePicture = ImageSource.Uri(event.newProfilePictureUri))
                    )
                }
            }
            is EditProfileEvent.ValidateAndSave -> {
                val currentProfile = _uiState.value.profile
                var isValid = true
                var newNameError: String? = null
                var newSurnameError: String? = null
                var newUsernameError: String? = null
                var newEmailError: String? = null

                if (currentProfile.name.isBlank()) {
                    newNameError = "Name is required"
                    isValid = false
                }

                if (currentProfile.surname.isBlank()) {
                    newSurnameError = "Surname is required"
                    isValid = false
                }

                if (currentProfile.username.isBlank()) {
                    newUsernameError = "Username is required"
                    isValid = false
                }

                if (currentProfile.email.isBlank()) {
                    newEmailError = "Email is required"
                    isValid = false
                } else if (!Patterns.EMAIL_ADDRESS.matcher(currentProfile.email).matches()) {
                    newEmailError = "Email format is not valid (es. name@domain.it)"
                    isValid = false
                }

                _uiState.update {
                    it.copy(
                        nameError = newNameError,
                        surnameError = newSurnameError,
                        usernameError = newUsernameError,
                        emailError = newEmailError
                    )
                }

                if (isValid) {
                    viewModelScope.launch {
                        val profilePic = currentProfile.profilePicture

                        if (profilePic is ImageSource.Uri && profilePic.uriString.isLocalUri()) {

                            val path = uploadProfilePictureUseCase(currentUserId, profilePic.uriString)

                            if (path != null) {
                                val finalProfile = currentProfile.copy(
                                    profilePictureUrl = path,
                                    profilePicture = ImageSource.Uri(path)
                                )
                                updateUserProfileUseCase(finalProfile)
                            } else {
                                updateUserProfileUseCase(currentProfile)
                            }
                        } else {
                            updateUserProfileUseCase(currentProfile)
                        }
                        event.onSuccess()
                    }
                }
            }
        }
    }

    private fun getAvailableFrames(currentLevel: Int): List<ProfileFrame> {
        return ProfileFrame.entries.filter { frame ->
            when(frame) {
                ProfileFrame.BRONZE -> true
                ProfileFrame.SILVER -> currentLevel >= 10
                ProfileFrame.GOLD -> currentLevel >= 25
                ProfileFrame.PLATINUM -> currentLevel >= 50
                ProfileFrame.DIAMOND -> currentLevel >= 100
            }
        }
    }
}