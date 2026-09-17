package com.example.tastify.profile.hub.logic

import com.example.tastify.models.Recipe
import com.example.tastify.models.UserProfile
import com.example.tastify.models.CustomCourse

data class ProfileHubState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val recentRecipes: List<Recipe> = emptyList(),
    val customCourses: List<CustomCourse> = emptyList(),
    val customCourseRecipes: List<Recipe> = emptyList(),
    val error: String? = null
)
