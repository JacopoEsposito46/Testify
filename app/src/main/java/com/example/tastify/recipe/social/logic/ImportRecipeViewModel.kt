package com.example.tastify.recipe.social.logic

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastify.models.ExtractedRecipeData
import com.example.tastify.recipe.social.domain.LlmRecipeExtractor
import com.example.tastify.recipe.social.domain.RecipeSource
import com.example.tastify.recipe.social.domain.SocialCaptionExtractor
import com.example.tastify.recipe.social.domain.UrlRecipeExtractor
import com.example.tastify.recipe.social.domain.CaptionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportRecipeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val urlRecipeExtractor: UrlRecipeExtractor,
    private val llmExtractor: LlmRecipeExtractor,
    private val captionExtractor: SocialCaptionExtractor
) : ViewModel() {

    private val _state = MutableStateFlow(ImportRecipeState())
    val state: StateFlow<ImportRecipeState> = _state.asStateFlow()

    fun onEvent(event: ImportRecipeEvent) {
        when (event) {
            is ImportRecipeEvent.OnUrlChanged -> {
                _state.value = _state.value.copy(urlInput = event.url, error = null)
            }
            is ImportRecipeEvent.OnImportClicked -> {
                importFromUrl(event.url)
            }
            is ImportRecipeEvent.OnSharedUrlReceived -> {
                _state.value = _state.value.copy(importUrlToConfirm = event.url)
            }
            is ImportRecipeEvent.ConfirmImportPreview -> {
                val url = _state.value.importUrlToConfirm
                _state.value = _state.value.copy(importUrlToConfirm = null)
                if (url != null) {
                    importFromUrl(url)
                }
            }
            is ImportRecipeEvent.DismissImportPreview -> {
                _state.value = _state.value.copy(importUrlToConfirm = null)
            }
            is ImportRecipeEvent.ClearExtractedData -> {
                _state.value = _state.value.copy(extractedData = null)
            }
            is ImportRecipeEvent.ClearError -> {
                _state.value = _state.value.copy(error = null)
            }
        }
    }

    fun importFromUrl(url: String) {
        if (url.isBlank() || !Patterns.WEB_URL.matcher(url).matches()) {
            _state.value = _state.value.copy(error = "Insert a valid URL")
            return
        }

        val source = urlRecipeExtractor.detectSource(url)
        if (source != null) {
            importFromSocialUrl(url, source)
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, loadingMessage = "Import in progress...", error = null)
            try {
                val extractedData = urlRecipeExtractor.extractRecipeData(url)
                if(extractedData.title == null && extractedData.ingredients.isEmpty() && extractedData.instructions.isEmpty()) {
                     _state.value = _state.value.copy(
                        isLoading = false,
                        loadingMessage = null,
                        error = "Unable to extract information from this URL."
                    )
                } else {
                     _state.value = _state.value.copy(
                        isLoading = false,
                        loadingMessage = null,
                        extractedData = extractedData
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    loadingMessage = null,
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    fun importFromSocialUrl(url: String, source: RecipeSource) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, loadingMessage = "Import in progress...", error = null)
            val captionResult = captionExtractor.fetchCaption(url, source)
            
            when (captionResult) {
                is CaptionResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, loadingMessage = null, error = captionResult.message)
                }
                is CaptionResult.Success -> {
                    _state.value = _state.value.copy(loadingMessage = "Analyzing recipe...")
                    val extracted = llmExtractor.extractRecipe(captionResult.caption)
                    if (extracted == null) {
                        _state.value = _state.value.copy(isLoading = false, loadingMessage = null, error = "No recipe found in the caption")
                    } else {
                        val finalData = extracted.copy(
                            url = url,
                            imageUrl = captionResult.thumbnailUrl ?: extracted.imageUrl
                        )
                        _state.value = _state.value.copy(isLoading = false, loadingMessage = null, extractedData = finalData)
                    }
                }
            }
        }
    }
}
