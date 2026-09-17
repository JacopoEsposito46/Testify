package com.example.tastify.data

import com.example.tastify.models.Ingredient
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {

    fun getIngredientsFlow(): Flow<List<Ingredient>>

    suspend fun searchIngredients(query: String): List<Ingredient>

    suspend fun getIngredientById(id: String)   : Ingredient?

    suspend fun addIngredient(ingredient: Ingredient)
}