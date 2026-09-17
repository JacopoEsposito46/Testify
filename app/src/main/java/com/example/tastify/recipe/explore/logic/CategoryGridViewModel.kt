package com.example.tastify.recipe.explore.logic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.tastify.domain.GetRecipesUseCase
import com.example.tastify.models.CuisineType
import com.example.tastify.models.DietaryRestriction
import com.example.tastify.models.Recipe
import com.example.tastify.models.RecipeDifficulty
import com.example.tastify.models.ExploreCategory
import com.example.tastify.navigation.ExploreCategoryRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CategoryGridState(
    val categoryTitle: String = "",
    val recipes: List<Recipe> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class CategoryGridViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getRecipesUseCase: GetRecipesUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<ExploreCategoryRoute>()
    private val category = route.category

    val uiState: StateFlow<CategoryGridState> = getRecipesUseCase()
        .map { allRecipes ->
            when (category) {
                ExploreCategory.POPULAR -> {
                    val popularRecipes = allRecipes
                        .sortedByDescending { it.rating }
                        .take(30)
                    CategoryGridState(
                        categoryTitle = "Most Popular Today",
                        recipes = popularRecipes,
                        isLoading = false
                    )
                }
                ExploreCategory.ITALIAN -> {
                    val italianRecipes = allRecipes.filter { it.cuisineType == CuisineType.ITALIAN }
                    CategoryGridState(
                        categoryTitle = "Italian Cuisine",
                        recipes = italianRecipes,
                        isLoading = false
                    )
                }
                ExploreCategory.VEGETARIAN -> {
                    val vegRecipes = allRecipes.filter { it.dietaryRestrictions.contains(DietaryRestriction.VEGETARIAN) }
                    CategoryGridState(
                        categoryTitle = "Vegetarian",
                        recipes = vegRecipes,
                        isLoading = false
                    )
                }
                ExploreCategory.GLUTEN_FREE -> {
                    val gfRecipes = allRecipes.filter { it.dietaryRestrictions.contains(DietaryRestriction.GLUTEN_FREE) }
                    CategoryGridState(
                        categoryTitle = "Gluten-Free",
                        recipes = gfRecipes,
                        isLoading = false
                    )
                }
                ExploreCategory.EASY -> {
                    val easyRecipes = allRecipes.filter { it.difficulty == RecipeDifficulty.EASY }
                    CategoryGridState(
                        categoryTitle = "Easy to Make",
                        recipes = easyRecipes,
                        isLoading = false
                    )
                }
                ExploreCategory.JAPANESE -> {
                    val japaneseRecipes = allRecipes.filter { it.cuisineType == CuisineType.JAPANESE }
                    CategoryGridState(
                        categoryTitle = "Japanese Cuisine",
                        recipes = japaneseRecipes,
                        isLoading = false
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CategoryGridState()
        )
}
