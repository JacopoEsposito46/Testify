package com.example.tastify.domain

import com.example.tastify.data.ProfileRepository
import com.example.tastify.models.UserProfile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(userId: String): Flow<UserProfile> {
        return profileRepository.getUserProfile(userId)
    }
}

class UpdateUserProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(profile: UserProfile) {
        profileRepository.updateUserProfile(profile)
    }
}

class UpdateCookedRecipesCountUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(userId: String, count: Int) {
        profileRepository.updateCookedRecipesCount(userId, count)
    }
}

class UploadProfilePictureUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(userId: String, localUri: String): String? {
        return profileRepository.uploadAndSaveProfilePicture(userId, localUri)
    }
}

class SetFollowingUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(currentUserId: String, targetUserId: String, follow: Boolean) {
        profileRepository.setFollowing(
            currentUserId = currentUserId,
            targetUserId = targetUserId,
            follow = follow
        )
    }
}
