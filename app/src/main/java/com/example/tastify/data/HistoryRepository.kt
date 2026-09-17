package com.example.tastify.data

import com.example.tastify.models.HistoryItem
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getHistory(userId: String, limit: Int): Flow<List<HistoryItem>>
    fun getDoneHistory(userId: String, limit: Int): Flow<List<HistoryItem>>
    fun isRecipeDone(userId: String, recipeId: String): Flow<Boolean>
    suspend fun addRecipeToHistory(userId: String, recipeId: String)
    suspend fun removeRecipeFromHistory(userId: String, recipeId: String)
    suspend fun addRecipeToDoneHistory(userId: String, recipeId: String)
    suspend fun removeRecipeFromDoneHistory(userId: String, recipeId: String)
}