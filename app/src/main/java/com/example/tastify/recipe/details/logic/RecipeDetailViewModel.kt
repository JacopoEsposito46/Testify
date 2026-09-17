package com.example.tastify.recipe.details.logic

import android.util.Log
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.tastify.domain.AddRecipeToCourse
import com.example.tastify.domain.AddRecipeToHistoryUseCase
import com.example.tastify.domain.DeleteRecipeUseCase
import com.example.tastify.domain.GetCoursesUseCase
import com.example.tastify.domain.GetRecipeByIdUseCase
import com.example.tastify.domain.TrackRecipeLikeUseCase
import com.example.tastify.domain.TrackRecipeViewUseCase
import com.example.tastify.navigation.RecipeProposalRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import com.example.tastify.notifications.SnackbarManager
import com.example.tastify.domain.CheckIfFavoriteUseCase
import com.example.tastify.domain.CheckIfRecipeDoneUseCase
import com.example.tastify.domain.IncrementRecipeViewsUseCase
import com.example.tastify.domain.ToggleFavoriteUseCase
import com.example.tastify.domain.UpdateRecipeVisibilityUseCase
import com.example.tastify.domain.ToggleRecipeDoneUseCase
import com.example.tastify.utils.PdfGenerator
import com.example.tastify.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle,
    private val getRecipeByIdUseCase: GetRecipeByIdUseCase,
    private val deleteRecipeUseCase: DeleteRecipeUseCase,
    private val addRecipeToHistoryUseCase: AddRecipeToHistoryUseCase,
    private val getCoursesUseCase: GetCoursesUseCase,
    private val addRecipeToCourseUseCase: AddRecipeToCourse,
    private val snackbarManager: SnackbarManager,
    private val checkIfFavoriteUseCase: CheckIfFavoriteUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val trackRecipeViewUseCase: TrackRecipeViewUseCase,
    private val trackRecipeLikeUseCase: TrackRecipeLikeUseCase,
    private val incrementRecipeViewsUseCase: IncrementRecipeViewsUseCase,
    private val updateRecipeVisibilityUseCase: UpdateRecipeVisibilityUseCase,
    private val checkIfRecipeDoneUseCase: CheckIfRecipeDoneUseCase,
    private val toggleRecipeDoneUseCase: ToggleRecipeDoneUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<RecipeProposalRoute>()
    private val recipeId = route.recipeId
    private val currentUserId = SessionManager.CURRENT_LOGGED_IN_USER_ID ?: ""

    init {
        if (currentUserId.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    addRecipeToHistoryUseCase(currentUserId, recipeId)
                } catch (exception: Exception) {
                    Log.e(TAG, "Unable to add recipe to history for recipeId=$recipeId", exception)
                }
            }
        }
    }

    val uiState: StateFlow<RecipeDetailState> = combine(
        getRecipeByIdUseCase(recipeId),
        getCoursesUseCase(currentUserId),
        checkIfFavoriteUseCase(currentUserId, recipeId),
        checkIfRecipeDoneUseCase(currentUserId, recipeId)
    ){ recipe, courses, isFavorite, isDone ->
        val isUserOwner = recipe?.authorId == currentUserId
        RecipeDetailState(
            selectedRecipe = recipe,
            isOwner = isUserOwner,
            isLoadingDetails = false,
            courses = courses,
            isFavorite = isFavorite,
            isDone = isDone
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecipeDetailState(isLoadingDetails = true)
    )

    fun onEvent(event: RecipeDetailEvent) {
        when (event) {
            is RecipeDetailEvent.RecipeOpened -> {
                viewModelScope.launch(Dispatchers.IO) {
                    uiState.filter { !it.isLoadingDetails && it.selectedRecipe != null }
                        .firstOrNull()?.let { state ->
                            if (!state.isOwner && savedStateHandle.get<Boolean>(VIEW_RECORDED_KEY) != true) {
                                savedStateHandle[VIEW_RECORDED_KEY] = true
                                try {
                                    incrementRecipeViewsUseCase(recipeId)
                                } catch (exception: Exception) {
                                    savedStateHandle[VIEW_RECORDED_KEY] = false
                                    Log.e(TAG, "Unable to record recipe view for recipeId=$recipeId", exception)
                                }
                            }
                            if (state.isOwner && state.selectedRecipe?.isPublic == false && state.selectedRecipe.reportCount >= 2) {
                                withContext(Dispatchers.Main) {
                                    snackbarManager.showMessage(
                                        text = "Recipe has too many reports to be public"
                                    )
                                }
                            }
                        }
                }
            }
            is RecipeDetailEvent.DeleteRecipe -> {
                viewModelScope.launch {
                    deleteRecipeUseCase(event.recipeId)
                    snackbarManager.showMessage("Recipe deleted")
                }
            }
            is RecipeDetailEvent.AddToCourse -> {
                viewModelScope.launch {
                    addRecipeToCourseUseCase(currentUserId, event.courseId, event.recipeId)
                    snackbarManager.showMessage("Recipe added to course")
                }
            }
            is RecipeDetailEvent.ToggleFavourite -> {
                viewModelScope.launch {
                    val currentFavoriteState = uiState.value.isFavorite
                    val authorId = uiState.value.selectedRecipe?.authorId

                    toggleFavoriteUseCase(currentUserId, recipeId, currentFavoriteState)

                    if (authorId != null && authorId != currentUserId) {
                        val isNowLiked = !currentFavoriteState
                        trackRecipeLikeUseCase(authorId, recipeId, isNowLiked)
                    }

                    snackbarManager.showMessage(
                        if (currentFavoriteState) "Removed from favorites"
                        else "Added to favorites"
                    )
                }
            }
            is RecipeDetailEvent.PublishRecipe -> {
                viewModelScope.launch {
                    val recipe = uiState.value.selectedRecipe
                    if (recipe != null && !recipe.isPublic) {
                        try {
                            updateRecipeVisibilityUseCase(recipeId, true)
                            snackbarManager.showMessage("Recipe published successfully!")
                        } catch (e: Exception) {
                            snackbarManager.showMessage("Cannot publish: recipe has too many reports to be public")
                        }
                    }
                }
            }
            is RecipeDetailEvent.UnpublishRecipe -> {
                viewModelScope.launch {
                    val recipe = uiState.value.selectedRecipe
                    if (recipe != null && recipe.isPublic) {
                        updateRecipeVisibilityUseCase(recipeId, false)
                        snackbarManager.showMessage("Recipe is now private")
                    }
                }
            }
            is RecipeDetailEvent.ToggleDone -> {
                viewModelScope.launch {
                    val currentDoneState = uiState.value.isDone
                    toggleRecipeDoneUseCase(currentUserId, recipeId, currentDoneState)
                    snackbarManager.showMessage(
                        if (currentDoneState) "Removed from cooked recipes"
                        else "Recipe marked as cooked"
                    )
                }
            }
            is RecipeDetailEvent.DownloadRecipePdf -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val recipe = uiState.value.selectedRecipe
                    if (recipe != null) {
                        try {
                            PdfGenerator.generateRecipePdf(context, event.uri, recipe, event.themeColors)
                            withContext(Dispatchers.Main) {
                                snackbarManager.showMessage("PDF download successfully!")
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                snackbarManager.showMessage("Error during download")
                            }
                        }
                    }
                }
            }
        }
    }

    private companion object {
        const val TAG = "RecipeDetailViewModel"
        const val VIEW_RECORDED_KEY = "recipe_view_recorded"
    }
}
