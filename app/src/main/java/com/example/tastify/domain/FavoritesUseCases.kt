package com.example.tastify.domain

import com.example.tastify.data.FavoritesRepository
import com.example.tastify.models.FavoriteReference
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CheckIfFavoriteUseCase @Inject constructor(private val repository: FavoritesRepository) {
    operator fun invoke(userId: String, recipeId: String): Flow<Boolean> {
        return repository.isRecipeFavorite(userId, recipeId)
    }
}

class ToggleFavoriteUseCase @Inject constructor(private val repository: FavoritesRepository) {
    suspend operator fun invoke(userId: String, recipeId: String, isCurrentlyFavorite: Boolean) {
        if (isCurrentlyFavorite) {
            repository.removeRecipeFromFavorites(userId, recipeId)
        } else {
            repository.addRecipeToFavorites(userId, recipeId)
        }
    }
}

class GetFavoriteRecipesReferencesUseCase @Inject constructor(private val repository: FavoritesRepository) {
    operator fun invoke(userId: String, limit: Int = 1000): Flow<List<FavoriteReference>> {
        return repository.getFavoriteRecipesReferences(userId, limit)
    }
}