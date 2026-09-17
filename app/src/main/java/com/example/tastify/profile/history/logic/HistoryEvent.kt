package com.example.tastify.profile.history.logic

sealed interface HistoryEvent {
    data object LoadMore : HistoryEvent
    data class SelectHistoryType(val historyType: HistoryType) : HistoryEvent
    data class RemoveRecipe(val recipeId: String) : HistoryEvent
}