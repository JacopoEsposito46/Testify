package com.example.tastify.data

import com.example.tastify.models.FavoriteReference
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun isRecipeFavorite(userId: String, recipeId: String): Flow<Boolean>
    fun getFavoriteRecipesReferences(userId: String, limit: Int): Flow<List<FavoriteReference>>
    suspend fun addRecipeToFavorites(userId: String, recipeId: String)
    suspend fun removeRecipeFromFavorites(userId: String, recipeId: String)
}