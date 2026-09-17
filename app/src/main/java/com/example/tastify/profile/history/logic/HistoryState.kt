package com.example.tastify.profile.history.logic

import com.example.tastify.models.Recipe

enum class HistoryType {
    VIEWED,
    DONE
}

data class HistoryUiItem(
    val recipe: Recipe,
    val historyText: String
)

data class HistoryState(
    val visibleRecipes: List<HistoryUiItem> = emptyList(),
    val canLoadMore: Boolean = false,
    val isLoading: Boolean = true,
    val selectedHistoryType: HistoryType = HistoryType.VIEWED
)