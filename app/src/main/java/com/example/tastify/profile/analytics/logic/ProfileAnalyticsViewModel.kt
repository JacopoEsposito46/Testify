package com.example.tastify.profile.analytics.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastify.domain.ProfileAnalyticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileAnalyticsViewModel @Inject constructor(
    private val profileAnalyticsUseCase: ProfileAnalyticsUseCase
) : ViewModel() {

    var state by mutableStateOf(ProfileAnalyticsState())
        private set

    fun onEvent(event: ProfileAnalyticsEvent) {
        when (event) {
            is ProfileAnalyticsEvent.CalculateAnalytics -> {
                loadFirebaseAnalytics(event.userId)
            }
        }
    }

    private fun loadFirebaseAnalytics(userId: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)

            profileAnalyticsUseCase(userId)
                .catch { exception ->
                    state = state.copy(
                        isLoading = false,
                        error = exception.message ?: "Error"
                    )
                }
                .collect { analytics ->
                    state = state.copy(
                        isLoading = false,
                        totalViews = analytics.totalViews,
                        totalLikes = analytics.totalLikes,
                        totalComments = analytics.totalComments,
                        recipesMade = analytics.recipesCount,
                        topRecipes = analytics.topRecipes,
                        weeklyTrendValues = analytics.weeklyTrend
                    )
                }
        }
    }
}