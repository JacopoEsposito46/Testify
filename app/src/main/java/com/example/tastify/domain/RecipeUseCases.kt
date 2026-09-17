package com.example.tastify.domain

import com.example.tastify.data.AnalyticsRepository
import com.example.tastify.data.RecipeRepository
import com.example.tastify.models.CuisineType
import com.example.tastify.models.DietaryRestriction
import com.example.tastify.models.Recipe
import com.example.tastify.models.RecipeDifficulty
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecipesUseCase @Inject constructor(private val repository: RecipeRepository) {
    operator fun invoke(ownerId: String? = null): Flow<List<Recipe>> {
        return if (ownerId != null) {
            repository.getRecipeByOwner(ownerId)
        } else {
            repository.getAllRecipes()
        }
    }
}

class GetFilteredRecipesUseCase @Inject constructor(private val repository: RecipeRepository) {
    operator fun invoke(
        searchQuery: String,
        difficulty: RecipeDifficulty?,
        cuisineTypes: Set<CuisineType>,
        dietaryRestrictions: Set<DietaryRestriction>,
        maxCalories: Float
    ): Flow<List<Recipe>> {
        return repository.getFilteredRecipes(
            searchQuery = searchQuery,
            difficulty = difficulty,
            cuisineTypes = cuisineTypes,
            dietaryRestrictions = dietaryRestrictions,
            maxCalories = maxCalories
        )
    }
}

class AddRecipeUseCase @Inject constructor(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipe: Recipe) {
        repository.addRecipe(recipe)
    }
}

class UpdateRecipeUseCase @Inject constructor(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipe: Recipe) {
        repository.updateRecipe(recipe)
    }
}

class DeleteRecipeUseCase @Inject constructor(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipeId: String) {
        repository.deleteRecipe(recipeId)
    }
}

class GetRecipeByIdUseCase @Inject constructor(private val repository: RecipeRepository) {
    operator fun invoke(recipeId: String): Flow<Recipe?> {
        return repository.getRecipeById(recipeId)
    }
}

class GetRecipesByIdsUseCase @Inject constructor(private val repository: RecipeRepository) {
    operator fun invoke(recipeIds: List<String>): Flow<List<Recipe>> {
        if (recipeIds.isEmpty()) return kotlinx.coroutines.flow.flowOf(emptyList())
        return repository.getRecipesByIds(recipeIds)
    }
}

class IncrementRecipeViewsUseCase @Inject constructor(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipeId: String) = repository.incrementViews(recipeId)
}

class UpdateRecipeRatingUseCase @Inject constructor(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipeId: String, newRating: Double) = repository.updateRecipeRating(recipeId, newRating)
}

class GetImportedRecipesUseCase @Inject constructor(private val repository: RecipeRepository) {
    operator fun invoke(ownerId: String): Flow<List<Recipe>> {
        return repository.getImportedRecipesByOwner(ownerId)
    }
}

class UpdateRecipeVisibilityUseCase @Inject constructor(private val repository: RecipeRepository) {
    suspend operator fun invoke(recipeId: String, isPublic: Boolean) {
        repository.updateRecipeVisibility(recipeId, isPublic)
    }
}
