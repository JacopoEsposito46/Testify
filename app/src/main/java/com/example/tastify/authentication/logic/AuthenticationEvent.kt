package com.example.tastify.authentication.logic

import android.content.Context
import com.example.tastify.models.CookingRole
import com.example.tastify.models.CuisineType
import com.example.tastify.models.DietaryRestriction

sealed interface AuthenticationEvent {
    data class OnAuthenticationClick(val context: Context) : AuthenticationEvent

    data class OnNicknameChange(val nickname: String) : AuthenticationEvent

    data class OnCuisineToggle(val cuisine: CuisineType) : AuthenticationEvent
    data class OnDietaryToggle(val dietary: DietaryRestriction) : AuthenticationEvent
    data class OnRoleToggle(val role: CookingRole) : AuthenticationEvent

    data object ClearNicknameError : AuthenticationEvent
    object OnSubmitRegistration : AuthenticationEvent
}