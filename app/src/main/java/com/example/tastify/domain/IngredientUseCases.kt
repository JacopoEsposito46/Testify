package com.example.tastify.domain

import com.example.tastify.data.IngredientRepository
import com.example.tastify.models.Ingredient
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllIngredientsUseCase @Inject constructor(
    private val repository: IngredientRepository
) {
    operator fun invoke(): Flow<List<Ingredient>> {
        return repository.getIngredientsFlow()
    }
}

class SearchIngredientsUseCase @Inject constructor(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(query: String): List<Ingredient> {
        return repository.searchIngredients(query)
    }
}

class GetOrCreateCustomIngredientUseCase @Inject constructor(
    private val repository: IngredientRepository
) {
    suspend operator fun invoke(name: String, imageUrl: String? = null): Ingredient {
        val cleanName = name.trim()

        val existing = repository.searchIngredients(cleanName).firstOrNull {
            it.name.equals(cleanName, ignoreCase = true)
        }

        if (existing != null) {
            return existing
        }

        val newIngredient = Ingredient(name = cleanName, defaultImage = imageUrl)
        repository.addIngredient(newIngredient)
        return newIngredient
    }
}