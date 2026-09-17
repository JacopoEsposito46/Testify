package com.example.tastify.recipe.courses.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastify.domain.AddRecipeToCourse
import com.example.tastify.domain.CreateCourse
import com.example.tastify.domain.DeleteCourse
import com.example.tastify.domain.GetCoursesUseCase
import com.example.tastify.domain.GetFavoriteRecipesReferencesUseCase
import com.example.tastify.domain.GetRecipesByIdsUseCase
import com.example.tastify.domain.GetImportedRecipesUseCase
import com.example.tastify.domain.GetRecipesUseCase
import com.example.tastify.domain.RemoveRecipeFromCourse
import com.example.tastify.domain.ToggleFavoriteUseCase
import com.example.tastify.models.CustomCourse
import com.example.tastify.models.FavoriteReference
import com.example.tastify.models.Recipe
import com.example.tastify.notifications.SnackbarManager
import com.example.tastify.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomCoursesViewModel @Inject constructor(
    private val getCoursesUseCase: GetCoursesUseCase,
    private val getFavoriteRecipesReferencesUseCase: GetFavoriteRecipesReferencesUseCase,
    private val getRecipesByIdsUseCase: GetRecipesByIdsUseCase,
    private val getImportedRecipesUseCase: GetImportedRecipesUseCase,
    private val getRecipesUseCase: GetRecipesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val createCourseUseCase: CreateCourse,
    private val addRecipeUseCase: AddRecipeToCourse,
    private val removeRecipeUseCase: RemoveRecipeFromCourse,
    private val deleteCourseUseCase: DeleteCourse,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomCoursesState())
    val uiState: StateFlow<CustomCoursesState> = _uiState.asStateFlow()
    private val currentUserId = SessionManager.CURRENT_LOGGED_IN_USER_ID ?: ""
    private var observeDataJob: Job? = null

    init {
        observeData()
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeData() {
        observeDataJob?.cancel()
        observeDataJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                getCoursesUseCase(currentUserId),
                getFavoriteRecipesReferencesUseCase(currentUserId),
                getImportedRecipesUseCase(currentUserId),
                getRecipesUseCase(currentUserId)
            ) { coursesList, favoritesList, importedList, myRecipes ->
                CoursesData(coursesList, favoritesList, importedList, myRecipes)
            }.flatMapLatest { data ->
                val coursesList = data.coursesList
                val favoritesList = data.favoritesList
                val importedList = data.importedList
                val myRecipes = data.myRecipes
                val favoriteIds = favoritesList.map { it.recipeId }
                val courseRecipeIds = coursesList.flatMap { it.recipeIds }
                val recipeIds = (favoriteIds + courseRecipeIds).distinct()
                val recipesFlow = if (recipeIds.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    getRecipesByIdsUseCase(recipeIds)
                }

                recipesFlow.map { recipesList ->
                    val accessibleRecipes = (recipesList + importedList + myRecipes).distinctBy { it.recipeId }
                    val recipesById = accessibleRecipes.associateBy { it.recipeId }
                    val favouriteRecipes = favoriteIds.mapNotNull { recipesById[it] }
                    val orderedRecipes = recipeIds.mapNotNull { recipesById[it] }

                    CustomCoursesState(
                        isLoading = false,
                        courses = coursesList,
                        favoriteCount = favouriteRecipes.size,
                        favouriteRecipes = favouriteRecipes,
                        savedRecipes = orderedRecipes,
                        favoriteRecipeIds = favoriteIds,
                        importedRecipes = importedList,
                        importedCount = importedList.size,
                        myRecipes = myRecipes
                    )
                }
            }.collect { nextState ->
                _uiState.update {
                    nextState
                }
            }
        }
    }

    private data class CoursesData(
        val coursesList: List<CustomCourse>,
        val favoritesList: List<FavoriteReference>,
        val importedList: List<Recipe>,
        val myRecipes: List<Recipe>
    )

    fun onEvent(event: CustomCoursesEvent) {
        viewModelScope.launch {
            when (event) {
                is CustomCoursesEvent.CreateCourse -> {
                    createCourseUseCase(currentUserId, event.title)
                }
                is CustomCoursesEvent.DeleteCourse -> {
                    deleteCourseUseCase(currentUserId, event.courseId)
                    snackbarManager.showMessage("Course deleted")
                }
                is CustomCoursesEvent.AddRecipeToCourse -> {
                    addRecipeUseCase(currentUserId, event.courseId, event.recipeId)
                    snackbarManager.showMessage("Recipe added to course")
                }
                is CustomCoursesEvent.RemoveRecipeFromCourse -> {
                    removeRecipeUseCase(currentUserId, event.courseId, event.recipeId)
                    snackbarManager.showMessage("Recipe removed from course")
                }
                is CustomCoursesEvent.ToggleFavourite -> {
                    val isCurrentlyFavorite = _uiState.value.favoriteRecipeIds.contains(event.recipeId)
                    toggleFavoriteUseCase(currentUserId, event.recipeId, isCurrentlyFavorite)
                    snackbarManager.showMessage(
                        if (isCurrentlyFavorite) "Removed from favorites"
                        else "Added to favorites"
                    )
                }
                CustomCoursesEvent.Refresh -> {
                    observeData()
                }
            }
        }
    }
}