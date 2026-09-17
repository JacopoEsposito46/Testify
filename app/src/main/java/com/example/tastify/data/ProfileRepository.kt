package com.example.tastify.data

import com.example.tastify.models.UserProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun getUserProfile(userId: String): Flow<UserProfile>
    suspend fun uploadAndSaveProfilePicture(userId: String, localUri: String): String?
    suspend fun updateUserProfile(profile: UserProfile)
    suspend fun updateCookedRecipesCount(userId: String, count: Int)
    suspend fun setFollowing(currentUserId: String, targetUserId: String, follow: Boolean)
    suspend fun addExperience(userId: String, expToAdd: Int)
}