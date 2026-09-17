package com.example.tastify.profile.logic

import com.example.tastify.models.UserProfile

data class ProfileState(
    val profile: UserProfile,
    val isLoading: Boolean = false,
    val totalUsageMinutes: Int = 0,
    val averageDailyUsageMinutes: Int = 0,
    val weeklyUsageMinutes: List<Int> = emptyList(),
    val usageAccessGranted: Boolean = false,
    val topIngredient: String = ""
)
