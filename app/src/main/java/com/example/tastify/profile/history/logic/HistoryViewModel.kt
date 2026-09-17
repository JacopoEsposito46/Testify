package com.example.tastify.profile.history.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastify.domain.GetDoneHistoryUseCase
import com.example.tastify.domain.GetHistoryUseCase
import com.example.tastify.domain.GetRecipesByIdsUseCase
import com.example.tastify.domain.RemoveRecipeFromDoneHistoryUseCase
import com.example.tastify.domain.RemoveRecipeFromHistoryUseCase
import com.example.tastify.utils.SessionManager
import com.example.tastify.utils.toHistoryRelativeTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val getDoneHistoryUseCase: GetDoneHistoryUseCase,
    private val getRecipesByIdsUseCase: GetRecipesByIdsUseCase,
    private val removeRecipeFromHistoryUseCase: RemoveRecipeFromHistoryUseCase,
    private val removeRecipeFromDoneHistoryUseCase: RemoveRecipeFromDoneHistoryUseCase
) : ViewModel() {

    private val userId = SessionManager.CURRENT_LOGGED_IN_USER_ID ?: ""

    private val _uiState = MutableStateFlow(HistoryState(isLoading = true))
    val uiState: StateFlow<HistoryState> = _uiState.asStateFlow()

    private val cachedHistory = mutableMapOf<HistoryType, CachedHistory>()
    private var loadJob: Job? = null

    init {
        loadHistory(HistoryType.VIEWED, INITIAL_LIMIT)
    }

    fun onEvent(event: HistoryEvent) {
        when (event) {
            is HistoryEvent.LoadMore -> {
                val type = _uiState.value.selectedHistoryType
                val nextLimit = (cachedHistory[type]?.loadedLimit ?: INITIAL_LIMIT) + PAGE_SIZE
                loadHistory(type = type, limit = nextLimit, forceRefresh = true)
            }
            is HistoryEvent.SelectHistoryType -> {
                loadHistory(event.historyType, cachedHistory[event.historyType]?.loadedLimit ?: INITIAL_LIMIT)
            }
            is HistoryEvent.RemoveRecipe -> {
                viewModelScope.launch {
                    val type = _uiState.value.selectedHistoryType
                    when (type) {
                        HistoryType.VIEWED -> removeRecipeFromHistoryUseCase(userId, event.recipeId)
                        HistoryType.DONE -> removeRecipeFromDoneHistoryUseCase(userId, event.recipeId)
                    }
                    removeCachedRecipe(type, event.recipeId)
                }
            }
        }
    }

    private fun loadHistory(
        type: HistoryType,
        limit: Int,
        forceRefresh: Boolean = false
    ) {
        val cached = cachedHistory[type]
        if (!forceRefresh && cached != null) {
            _uiState.value = cached.toState(type)
            return
        }

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = HistoryState(
                isLoading = true,
                selectedHistoryType = type
            )

            val historyItemsAll = when (type) {
                HistoryType.VIEWED -> getHistoryUseCase(userId, limit + LOAD_MORE_LOOKAHEAD)
                HistoryType.DONE -> getDoneHistoryUseCase(userId, limit + LOAD_MORE_LOOKAHEAD)
            }.first()

            if (historyItemsAll.isEmpty()) {
                cacheAndShow(
                    type = type,
                    history = CachedHistory(
                        items = emptyList(),
                        canLoadMore = false,
                        loadedLimit = limit
                    )
                )
                return@launch
            }

            val recipeIdsToFetch = historyItemsAll.map { it.recipeId }
            val fetchedRecipes = getRecipesByIdsUseCase(recipeIdsToFetch).first()

            val allFilteredItems = historyItemsAll.mapNotNull { item ->
                val recipe = fetchedRecipes.find { it.recipeId == item.recipeId }
                if (recipe != null && recipe.authorId != userId) {
                    HistoryUiItem(
                        recipe = recipe,
                        historyText = "${type.label} ${item.timestamp.toHistoryRelativeTime()}"
                    )
                } else null
            }

            val canLoadMore = allFilteredItems.size > limit
            val visibleItems = allFilteredItems.take(limit)

            cacheAndShow(
                type = type,
                history = CachedHistory(
                    items = visibleItems,
                    canLoadMore = canLoadMore,
                    loadedLimit = limit
                )
            )
        }
    }

    private fun cacheAndShow(type: HistoryType, history: CachedHistory) {
        cachedHistory[type] = history
        _uiState.value = history.toState(type)
    }

    private fun removeCachedRecipe(type: HistoryType, recipeId: String) {
        val cached = cachedHistory[type] ?: return
        val updated = cached.copy(
            items = cached.items.filterNot { item -> item.recipe.recipeId == recipeId }
        )
        cacheAndShow(type, updated)
    }

    private fun CachedHistory.toState(type: HistoryType) = HistoryState(
        visibleRecipes = items,
        canLoadMore = canLoadMore,
        isLoading = false,
        selectedHistoryType = type
    )

    private data class CachedHistory(
        val items: List<HistoryUiItem>,
        val canLoadMore: Boolean,
        val loadedLimit: Int
    )

    private companion object {
        const val INITIAL_LIMIT = 5
        const val PAGE_SIZE = 5
        const val LOAD_MORE_LOOKAHEAD = PAGE_SIZE
    }

    private val HistoryType.label: String
        get() = when (this) {
            HistoryType.VIEWED -> "Seen"
            HistoryType.DONE -> "Cooked"
        }
}
