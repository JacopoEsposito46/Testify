package com.example.tastify.recipe.social.logic

import com.example.tastify.models.ExtractedRecipeData

data class ImportRecipeState(
    val urlInput: String = "",
    val isLoading: Boolean = false,
    val loadingMessage: String? = null,
    val extractedData: ExtractedRecipeData? = null,
    val importUrlToConfirm: String? = null,
    val error: String? = null
)
