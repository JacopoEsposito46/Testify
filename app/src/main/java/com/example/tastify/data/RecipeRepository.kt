package com.example.tastify.data

import com.example.tastify.models.CuisineType
import com.example.tastify.models.DietaryRestriction
import com.example.tastify.models.Recipe
import com.example.tastify.models.RecipeDifficulty
import kotlinx.coroutines.flow.Flow

interface RecipeRepository{
    fun getAllRecipes(): Flow<List<Recipe>>
    fun getRecipeByOwner(ownerId: String): Flow<List<Recipe>>
    fun getImportedRecipesByOwner(ownerId: String): Flow<List<Recipe>>
    suspend fun addRecipe(recipe: Recipe)
    suspend fun updateRecipe(recipe: Recipe)
    suspend fun deleteRecipe(recipeId: String)
    suspend fun updateRecipeVisibility(recipeId: String, isPublic: Boolean)
    suspend fun updateRecipeRating(recipeId: String, newRating: Double)
    fun getRecipeById(recipeId: String): Flow<Recipe?>
    fun getFilteredRecipes(searchQuery: String, difficulty: RecipeDifficulty?, cuisineTypes: Set<CuisineType>, dietaryRestrictions: Set<DietaryRestriction>, maxCalories: Float): Flow<List<Recipe>>
    fun getRecipesByIds(recipeIds: List<String>): Flow<List<Recipe>>
    suspend fun incrementViews(recipeId: String)
}