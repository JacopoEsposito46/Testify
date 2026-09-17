package com.example.tastify.domain

import com.example.tastify.data.CustomCoursesRepository
import com.example.tastify.data.FavoritesRepository
import com.example.tastify.data.ProfileRepository
import com.example.tastify.data.RecipeRepository
import com.example.tastify.models.NotificationType
import com.example.tastify.models.Recipe
import com.example.tastify.models.UserProfile
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendationEngine @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val recipeRepository: RecipeRepository,
    private val notificationUseCases: NotificationUseCases,
    private val favoritesRepository: FavoritesRepository,
    private val customCoursesRepository: CustomCoursesRepository
) {

    suspend fun checkAndRecommend(userId: String) {
        val profile: UserProfile = try {
            profileRepository.getUserProfile(userId).first()
        } catch (_: Exception) {
            return
        }

        // no cuisine preferences -> no recommendation
        if (profile.favoritesCuisineType.isEmpty()) return

        // Get already-recommended recipe IDs to avoid duplicates
        val alreadyRecommendedRecipeIds: Set<String> = try {
            val notifications = notificationUseCases.getNotifications(userId).first()
            notifications
                .filter { it.type == NotificationType.RECOMMENDATION }
                .mapNotNull { it.relatedRecipeId }
                .toSet()
        } catch (_: Exception) {
            emptySet()
        }

        // Get favorite recipe IDs
        val favoriteRecipeIds: Set<String> = try {
            favoritesRepository.getFavoriteRecipesReferences(userId, Int.MAX_VALUE).first()
                .map { it.recipeId }
                .toSet()
        } catch (_: Exception) {
            emptySet()
        }

        // Get recipe IDs saved in user's custom courses
        val courseRecipeIds: Set<String> = try {
            customCoursesRepository.getCustomCourses(userId).first()
                .flatMap { it.recipeIds }
                .toSet()
        } catch (_: Exception) {
            emptySet()
        }

        val excludedRecipeIds = alreadyRecommendedRecipeIds + favoriteRecipeIds + courseRecipeIds

        val allRecipes: List<Recipe> = try {
            recipeRepository.getAllRecipes().first()
        } catch (_: Exception) {
            emptyList()
        }

        // Find best candidate
        val candidate = allRecipes
            .filter { recipe ->
                recipe.authorId != userId &&
                recipe.recipeId !in excludedRecipeIds &&
                recipe.cuisineType in profile.favoritesCuisineType
            }
            .sortedByDescending { it.rating }
            .firstOrNull()

        if (candidate != null) {
            notificationUseCases.notifyRecommendation(
                userId = userId,
                recipeTitle = candidate.title,
                recipeId = candidate.recipeId
            )
        }
    }
}
