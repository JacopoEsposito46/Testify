package com.example.tastify.recipe.details.logic

import android.net.Uri
import com.example.tastify.utils.PdfThemeColors

sealed class RecipeDetailEvent {
    object RecipeOpened : RecipeDetailEvent()
    data class DeleteRecipe(val recipeId: String) : RecipeDetailEvent()
    data class AddToCourse(val courseId: String, val recipeId: String) : RecipeDetailEvent()
    object ToggleFavourite : RecipeDetailEvent()
    data class DownloadRecipePdf(val uri: Uri, val themeColors: PdfThemeColors) : RecipeDetailEvent()
    object ToggleDone : RecipeDetailEvent()
    object PublishRecipe : RecipeDetailEvent()
    object UnpublishRecipe : RecipeDetailEvent()
}