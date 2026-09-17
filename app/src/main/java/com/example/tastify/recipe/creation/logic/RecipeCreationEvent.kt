package com.example.tastify.recipe.creation.logic

import com.example.tastify.models.Recipe

sealed interface RecipeCreationEvent {
    data class UpdateDraftRecipe(val recipe: Recipe) : RecipeCreationEvent
    data class ResetDraftRecipe(val authorId: String) : RecipeCreationEvent
    data class SaveRecipe(val isEditing: Boolean, val isCopying: Boolean, val isPrivate: Boolean = false) : RecipeCreationEvent

    data object ClearCoverImageError : RecipeCreationEvent
    data object ClearTitleError : RecipeCreationEvent
    data object ClearDescriptionError : RecipeCreationEvent
    data object ClearTimeError : RecipeCreationEvent
    data object ClearCaloriesError : RecipeCreationEvent
    data object ClearServesError : RecipeCreationEvent
    data object ClearIngredientsError : RecipeCreationEvent
    data class ClearIngredientNameError(val index: Int) : RecipeCreationEvent
    data class ClearIngredientQuantityError(val index: Int) : RecipeCreationEvent
    data class ClearIngredientUnitError(val index: Int) : RecipeCreationEvent
    data object ClearStepsError : RecipeCreationEvent
    data class ClearStepImageError(val index: Int) : RecipeCreationEvent
    data class ClearStepTitleError(val index: Int) : RecipeCreationEvent
    data class ClearStepDescriptionError(val index: Int) : RecipeCreationEvent
    data object ClearCopyError : RecipeCreationEvent
}
