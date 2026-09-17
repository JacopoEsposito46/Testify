package com.example.tastify.domain

import com.example.tastify.data.ProfileRepository
import javax.inject.Inject

class AddExperienceUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(userId: String, expToAdd: Int) {
        require(expToAdd > 0)
        profileRepository.addExperience(userId, expToAdd)
    }
}