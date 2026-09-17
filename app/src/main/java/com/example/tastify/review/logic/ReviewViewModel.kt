package com.example.tastify.review.logic

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastify.domain.AddReplyToReviewUseCase
import com.example.tastify.domain.AddReviewUseCase
import com.example.tastify.domain.DeleteReviewUseCase
import com.example.tastify.domain.GetRecipeByIdUseCase
import com.example.tastify.domain.GetReviewsUseCase
import com.example.tastify.domain.GetUserProfileUseCase
import com.example.tastify.domain.NotificationUseCases
import com.example.tastify.domain.UpdateRecipeRatingUseCase
import com.example.tastify.models.ProfileFrame
import com.example.tastify.models.Recipe
import com.example.tastify.models.Review
import com.example.tastify.notifications.SnackbarManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class ReviewViewModel @Inject constructor(
    val addReviewUseCase: AddReviewUseCase,
    val addReplyToReviewUseCase: AddReplyToReviewUseCase,
    val deleteReviewUseCase: DeleteReviewUseCase,
    val getReviewsUseCase: GetReviewsUseCase,
    private val getRecipeByIdUseCase: GetRecipeByIdUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val updateRecipeRatingUseCase: UpdateRecipeRatingUseCase,
    private val notificationUseCases: NotificationUseCases,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val _state = MutableStateFlow(ReviewState())
    val state: StateFlow<ReviewState> = _state.asStateFlow()
    private val _recipe = MutableStateFlow<Recipe?>(null)
    val recipe = _recipe.asStateFlow()

    private var recipeJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadRecipe(id: String) {
        recipeJob?.cancel()
        recipeJob = getRecipeByIdUseCase(id)
            .onEach { recipe -> _recipe.value = recipe }
            .filterNotNull()
            .flatMapLatest { recipe ->
                getUserProfileUseCase(recipe.authorId)
            }
            .onEach { profile ->
                if (profile != null) {
                    _state.update {
                        it.copy(
                            recipeAuthorNickname = profile.username.ifBlank { profile.name },
                            recipeAuthorProfileImageUri = profile.profilePictureUrl ?: ""
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }
    private var reviewsJob: Job? = null
    private var isSubmittingReview = false

    fun onEvent(event: ReviewEvent) {
        when (event) {
            is ReviewEvent.LoadReviews -> {
                observeReviews(event.recipeId)
                loadRecipe(event.recipeId)
            }
            is ReviewEvent.UpdateDraftReview -> _state.update { it.copy(draftReviewRating = event.rating, draftReviewText = event.text) }
            is ReviewEvent.AddDraftReviewImage -> _state.update { it.copy(draftReviewImageUris = it.draftReviewImageUris + event.uri) }
            is ReviewEvent.RemoveDraftReviewImage -> _state.update { it.copy(draftReviewImageUris = it.draftReviewImageUris - event.uri) }
            is ReviewEvent.SubmitReview -> submitReview(
                event.authorId,
                event.authorNickname,
                event.authorProfileImageUri,
                event.authorFrame,
                event.authorRole
            )
            is ReviewEvent.SubmitAuthorReply -> submitAuthorReply(event.reviewId, event.replyText)
            is ReviewEvent.DeleteReview -> deleteReview(event.reviewId)
            is ReviewEvent.ClearError -> _state.update { it.copy(reviewError = null) }
        }
    }

    private fun observeReviews(recipeId: String) {
        _state.update { it.copy(recipeId = recipeId, isLoading = true) }
        reviewsJob?.cancel()
        reviewsJob = getReviewsUseCase(recipeId)
            .onEach { reviews ->
                val avg = if (reviews.isEmpty()) 0.0 else "%.1f".format(reviews.map { it.rating }.average()).replace(",", ".").toDouble()
                _state.update { it.copy(reviews = reviews, averageRating = avg, isLoading = false) }
                updateRecipeRatingIfNeeded(recipeId, avg)
            }
            .launchIn(viewModelScope)
    }

    private suspend fun updateRecipeRatingIfNeeded(recipeId: String, averageRating: Double) {
        val currentRating = _recipe.value?.rating
        if (currentRating == null || kotlin.math.abs(currentRating - averageRating) >= 0.05) {
            updateRecipeRatingUseCase(recipeId, averageRating)
        }
    }

    private fun submitReview(
        authorId: String,
        authorNickname: String,
        authorProfileImageUri: String,
        authorFrame: ProfileFrame,
        authorRole: String
    ) {
        val stateVal = _state.value
        if (stateVal.recipeId.isBlank() || isSubmittingReview) return

        if (validateReview(stateVal.draftReviewRating, stateVal.draftReviewText, stateVal.draftReviewImageUris)) {
            isSubmittingReview = true
            val review = Review(
                recipeId = stateVal.recipeId,
                authorId = authorId,
                authorNickname = authorNickname,
                authorProfileImageUri = authorProfileImageUri,
                authorFrame = authorFrame,
                authorRole = authorRole,
                rating = stateVal.draftReviewRating,
                text = stateVal.draftReviewText.trim(),
                imageUris = stateVal.draftReviewImageUris
            )
            viewModelScope.launch {
                try {
                    addReviewUseCase(review)
                    val currentRecipe = _recipe.value
                    if (currentRecipe != null && currentRecipe.authorId != authorId) {
                        notificationUseCases.notifyNewReview(
                            recipeAuthorId = currentRecipe.authorId,
                            reviewerName = authorNickname,
                            recipeTitle = currentRecipe.title,
                            recipeId = stateVal.recipeId,
                            rating = stateVal.draftReviewRating
                        )
                    }
                    _state.update { it.copy(draftReviewRating = 0, draftReviewText = "", draftReviewImageUris = emptyList(), reviewError = null, submitSuccess = true) }
                    snackbarManager.showMessage("Review submitted successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Review submit failed", e)
                    _state.update { it.copy(reviewError = "Review image upload failed. Check Supabase storage policies.") }
                    snackbarManager.showMessage("Review image upload failed. Check Supabase storage policies.")
                } finally {
                    isSubmittingReview = false
                }
            }
        }
    }

    private fun deleteReview(reviewId: String) {
        val recipeId = _state.value.recipeId
        if (recipeId.isNotBlank()) {
            viewModelScope.launch {
                deleteReviewUseCase(recipeId, reviewId)
                snackbarManager.showMessage("Review deleted")
            }
        }
    }

    private fun validateReview(rating: Int, text: String, imageUris: List<String>): Boolean {
        var isValid = true
        var err: String? = null

        if (rating !in 1..5) {
            err = "The review rating must be a number between 1 and 5."
            isValid = false
        } else if (text.trim().isEmpty()) {
            err = "The review text cannot be blank."
            isValid = false
        } else if (imageUris.size > 3) {
            err = "You cannot attach more than 3 images to the review."
            isValid = false
        }

        _state.update { it.copy(reviewError = err) }
        return isValid
    }

    private fun submitAuthorReply(reviewId: String, replyText: String?) {
        val recipeId = _state.value.recipeId
        if (recipeId.isNotBlank()) {
            if (replyText != null && replyText.isBlank()) return

            viewModelScope.launch {
                try {
                    addReplyToReviewUseCase(recipeId, reviewId, replyText)
                    val message = if (replyText == null) "Reply deleted." else "Reply posted successfully."
                    snackbarManager.showMessage(message)
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing reply", e)
                    snackbarManager.showMessage("Failed to process reply.")
                }
            }
        }
    }

    private companion object {
        const val TAG = "ReviewViewModel"
    }
}