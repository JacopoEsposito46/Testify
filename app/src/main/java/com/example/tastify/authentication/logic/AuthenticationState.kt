package com.example.tastify.authentication.logic

import com.example.tastify.models.CookingRole
import com.example.tastify.models.CuisineType
import com.example.tastify.models.DietaryRestriction

data class AuthenticationState(
    val showRegistrationForm: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,

    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val photoUrl: String = "",

    val nickname: String = "",
    val nicknameError: String? = null,
    val selectedCuisine: List<CuisineType> = emptyList(),
    val selectedDietary: List<DietaryRestriction> = emptyList(),
    val selectedRole: CookingRole = CookingRole.BEGINNER,

    val isAuthSuccessful: Boolean = false
)