package com.example.tastify.domain

import com.example.tastify.data.HistoryRepository
import com.example.tastify.models.HistoryItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddRecipeToHistoryUseCase @Inject constructor(private val repository: HistoryRepository) {
    suspend operator fun invoke(userId: String, recipeId: String) = repository.addRecipeToHistory(userId, recipeId)
}

class GetHistoryUseCase @Inject constructor(private val repository: HistoryRepository) {
    operator fun invoke(userId: String, limit : Int): Flow<List<HistoryItem>> = repository.getHistory(userId, limit)
}

class GetDoneHistoryUseCase @Inject constructor(private val repository: HistoryRepository) {
    operator fun invoke(userId: String, limit : Int): Flow<List<HistoryItem>> = repository.getDoneHistory(userId, limit)
}

class CheckIfRecipeDoneUseCase @Inject constructor(private val repository: HistoryRepository) {
    operator fun invoke(userId: String, recipeId: String): Flow<Boolean> = repository.isRecipeDone(userId, recipeId)
}

class ToggleRecipeDoneUseCase @Inject constructor(private val repository: HistoryRepository) {
    suspend operator fun invoke(userId: String, recipeId: String, isCurrentlyDone: Boolean) {
        if (isCurrentlyDone) {
            repository.removeRecipeFromDoneHistory(userId, recipeId)
        } else {
            repository.addRecipeToDoneHistory(userId, recipeId)
        }
    }
}

class RemoveRecipeFromHistoryUseCase @Inject constructor(private val repository: HistoryRepository) {
    suspend operator fun invoke(userId: String, recipeId: String) = repository.removeRecipeFromHistory(userId, recipeId)
}

class RemoveRecipeFromDoneHistoryUseCase @Inject constructor(private val repository: HistoryRepository) {
    suspend operator fun invoke(userId: String, recipeId: String) = repository.removeRecipeFromDoneHistory(userId, recipeId)
}