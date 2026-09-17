package com.example.tastify.recipe.explore.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastify.domain.GetRecipesUseCase
import com.example.tastify.domain.GetFavoriteRecipesReferencesUseCase
import com.example.tastify.domain.RecommendationEngine
import com.example.tastify.domain.ToggleFavoriteUseCase
import com.example.tastify.notifications.SnackbarManager
import com.example.tastify.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class RecipeExploreViewModel @Inject constructor(
    private val getRecipesUseCase: GetRecipesUseCase,
    private val getFavoriteRecipesReferencesUseCase: GetFavoriteRecipesReferencesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val recommendationEngine: RecommendationEngine,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filters = MutableStateFlow(RecipeExploreState())
    private val _isFiltering = MutableStateFlow(false)
    val currentSearchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val currentUserId = SessionManager.CURRENT_LOGGED_IN_USER_ID ?: ""

    init {
        // Trigger recommendation check once per session
        viewModelScope.launch {
            delay(20000L)
            recommendationEngine.checkAndRecommend(currentUserId)
        }
    }

    val uiState: StateFlow<RecipeExploreState> = combine(
        _searchQuery.debounce { if (it.isEmpty()) 0L else 300L },
        _filters,
        getRecipesUseCase(),
        getFavoriteRecipesReferencesUseCase(currentUserId),
        _isFiltering.asStateFlow()
    ) { query, filters, allRecipes, favoritesList, isFilteringNow ->

        val favoriteIdsSet = favoritesList.map { it.recipeId }.toSet()

        val filteredRecipes = allRecipes.filter { recipe ->
            val isNotOwnRecipe = recipe.authorId != currentUserId
            val normalizedQuery = query.replace(" ", "").lowercase()
            val normalizedTitle = recipe.title.replace(" ", "").lowercase()

            val matchesQuery = query.isBlank() || normalizedTitle.contains(normalizedQuery)
            val matchesDifficulty = filters.selectedDifficulty == null || recipe.difficulty == filters.selectedDifficulty
            val matchesServings = recipe.serves >= filters.servings

            val matchesCost = if (filters.costRange.endInclusive >= 5f) {
                recipe.cost.toFloat() >= filters.costRange.start
            } else {
                recipe.cost.toFloat() in filters.costRange
            }

            val matchesCalories = filters.maxCalories >= 10000f || recipe.calories <= filters.maxCalories

            val matchesDietary = filters.selectedDietaryRestrictions.isEmpty() ||
                    recipe.dietaryRestrictions.containsAll(filters.selectedDietaryRestrictions)
            val matchesCuisine = filters.selectedCuisineTypes.isEmpty() ||
                    filters.selectedCuisineTypes.contains(recipe.cuisineType)
            val matchesIngredients = filters.includedIngredients.isEmpty() ||
                    filters.includedIngredients.all { filterIngredient ->
                        val normalizedFilter = filterIngredient.normalizedIngredientFilter()
                        recipe.ingredients.any { recipeIngredient ->
                            recipeIngredient.ingredient.name
                                .normalizedIngredientFilter()
                                .contains(normalizedFilter, ignoreCase = true)
                        }
                    }

            isNotOwnRecipe && matchesQuery && matchesDifficulty && matchesServings && matchesCost &&
                    matchesCalories && matchesDietary && matchesCuisine && matchesIngredients
        }

        filters.copy(
            searchQuery = _searchQuery.value,
            isLoading = false,
            isFiltering = isFilteringNow,
            recipes = filteredRecipes,
            favoriteRecipeIds = favoriteIdsSet
        )
    }
        .flowOn(kotlinx.coroutines.Dispatchers.Default)
        .onEach {
            _isFiltering.value = false
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RecipeExploreState()
        )

    fun onEvent(event: RecipeExploreEvent) {
        when (event) {
            is RecipeExploreEvent.UpdateSearchQuery -> {
                if (_searchQuery.value != event.newQuery) {
                    _isFiltering.value = true
                    _searchQuery.value = event.newQuery
                }
            }
            is RecipeExploreEvent.UpdateDifficulty -> _filters.update { it.copy(selectedDifficulty = event.difficulty) }
            is RecipeExploreEvent.UpdateServings -> _filters.update { it.copy(servings = event.servings) }
            is RecipeExploreEvent.UpdateCostRange -> _filters.update { it.copy(costRange = event.range) }
            is RecipeExploreEvent.UpdateMaxCalories -> _filters.update { it.copy(maxCalories = event.calories) }
            is RecipeExploreEvent.ToggleDietaryRestriction -> _filters.update { state ->
                val newSet = if (state.selectedDietaryRestrictions.contains(event.restriction)) {
                    state.selectedDietaryRestrictions - event.restriction
                } else {
                    state.selectedDietaryRestrictions + event.restriction
                }
                state.copy(selectedDietaryRestrictions = newSet)
            }
            is RecipeExploreEvent.ToggleCuisineType -> _filters.update { state ->
                val newSet = if (state.selectedCuisineTypes.contains(event.cuisineType)) {
                    state.selectedCuisineTypes - event.cuisineType
                } else {
                    state.selectedCuisineTypes + event.cuisineType
                }
                state.copy(selectedCuisineTypes = newSet)
            }
            is RecipeExploreEvent.AddIngredient -> _filters.update { state ->
                val ingredient = event.ingredient.normalizedIngredientFilter()
                val alreadyAdded = state.includedIngredients.any { it.equals(ingredient, ignoreCase = true) }

                if (ingredient.isBlank() || alreadyAdded) {
                    state
                } else {
                    state.copy(includedIngredients = state.includedIngredients + ingredient)
                }
            }
            is RecipeExploreEvent.RemoveIngredient -> _filters.update { state ->
                val ingredient = event.ingredient.normalizedIngredientFilter()
                state.copy(
                    includedIngredients = state.includedIngredients
                        .filterNot { it.equals(ingredient, ignoreCase = true) }
                        .toSet()
                )
            }
            is RecipeExploreEvent.ToggleFavourite -> {
                viewModelScope.launch {
                    val isFavorite = uiState.value.favoriteRecipeIds.contains(event.recipeId)
                    toggleFavoriteUseCase(currentUserId, event.recipeId, isFavorite)
                    snackbarManager.showMessage(
                        if (isFavorite) "Removed from favorites"
                        else "Added to favorites"
                    )
                }
            }
            is RecipeExploreEvent.ResetFilters -> _filters.value = RecipeExploreState()
        }
    }

    private fun String.normalizedIngredientFilter(): String {
        return trim().replace(WHITESPACE_REGEX, " ")
    }

    private companion object {
        val WHITESPACE_REGEX = "\\s+".toRegex()
    }
}