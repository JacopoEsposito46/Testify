package com.example.tastify.profile.edit.logic

import com.example.tastify.models.UserProfile

sealed interface EditProfileEvent {
    data class UpdateProfile(val updatedProfile: UserProfile) : EditProfileEvent
    data class UpdateProfilePicture(val newProfilePictureUri: String) : EditProfileEvent
    data class ValidateAndSave(val onSuccess: () -> Unit) : EditProfileEvent
}