package com.example.tastify.profile.analytics.logic

import com.example.tastify.models.Recipe

data class ProfileAnalyticsState(
    val isLoading: Boolean = false,
    val totalViews: Int = 0,
    val totalLikes: Int = 0,
    val totalComments: Int = 0,
    val recipesMade: Int = 0,
    val topRecipes: List<Recipe> = emptyList(),
    val weeklyTrendValues: List<Float> = emptyList(),
    val error: String? = null
)