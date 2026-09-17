package com.example.tastify.recipe.social.logic

sealed class ImportRecipeEvent {
    data class OnUrlChanged(val url: String) : ImportRecipeEvent()
    data class OnImportClicked(val url: String) : ImportRecipeEvent()
    data class OnSharedUrlReceived(val url: String) : ImportRecipeEvent()
    object ConfirmImportPreview : ImportRecipeEvent()
    object DismissImportPreview : ImportRecipeEvent()
    object ClearExtractedData : ImportRecipeEvent()
    object ClearError : ImportRecipeEvent()
}
