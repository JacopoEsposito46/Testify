package com.example.tastify.profile.edit.logic

import com.example.tastify.models.UserProfile
import com.example.tastify.models.CookingRole
import com.example.tastify.models.ProfileFrame

data class EditProfileState (
    val profile: UserProfile,
    val isLoading: Boolean = true,
    val nameError: String? = null,
    val surnameError: String? = null,
    val usernameError: String? = null,
    val emailError: String? = null,
    val availableRoles: List<CookingRole> = emptyList(),
    val availableFrames: List<ProfileFrame> = emptyList()
)