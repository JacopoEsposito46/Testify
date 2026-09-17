package com.example.tastify.recipe.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tastify.components.SharedTopBar
import com.example.tastify.recipe.stats.components.StatsParameters
import com.example.tastify.recipe.stats.components.StatsSummary
import com.example.tastify.recipe.stats.components.WeeklyTrend
import com.example.tastify.recipe.stats.logic.RecipeStatsEvent
import com.example.tastify.recipe.stats.logic.RecipeStatsViewModel

@Composable
fun RecipeStatistics(
    viewModel: RecipeStatsViewModel,
    onBack: () -> Unit
){
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SharedTopBar(
                title = "Recipe Statistics",
                onBack = onBack,
                actions = {}
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatsSummary(
                title = state.recipeTitle,
                totalViews = state.totalViews,
                viewsChangeText = state.viewsChangeText
            )

            StatsParameters(
                totalFavorites = state.totalFavorites,
                totalComments = state.totalComments,
                totalReviews = state.totalReviews,
                averageRating = state.averageRating,
                favoritesChangeText = state.favoritesChangeText,
                commentsChangeText = state.commentsChangeText,
                reviewsChangeText = state.reviewsChangeText
            )

            WeeklyTrend(
                selectedPeriod = state.selectedPeriod,
                trendPoints = state.trendPoints,
                onPeriodSelected = { period ->
                    viewModel.onEvent(RecipeStatsEvent.ChangePeriod(period))
                }
            )
        }

    }
}