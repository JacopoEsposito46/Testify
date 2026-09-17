package com.example.tastify.profile.hub.logic

import com.example.tastify.models.Recipe

sealed class ProfileHubEvent {
    data class LoadHubData(val userId: String) : ProfileHubEvent()
    data class OnRecipeClicked(val recipe: Recipe) : ProfileHubEvent()
    data class SetFollowing(val currentUserId: String, val targetUserId: String, val follow: Boolean) : ProfileHubEvent()
}