package com.example.tastify.profile.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tastify.components.SharedTopBar
import com.example.tastify.profile.analytics.components.AnalyticsStatCard
import com.example.tastify.profile.analytics.components.AnalyticsTrendChart
import com.example.tastify.profile.analytics.components.TopRecipesSection
import com.example.tastify.profile.analytics.logic.ProfileAnalyticsState

@Composable
fun ProfileAnalyticsScreen(
    state: ProfileAnalyticsState,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            SharedTopBar(
                title = "Profile Analytics",
                onBack = onBack,
                actions = {}
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AnalyticsStatCard(
                            label = "Total Views",
                            value = "%,d".format(state.totalViews),
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticsStatCard(
                            label = "Recipes Made",
                            value = state.recipesMade.toString(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AnalyticsStatCard(
                            label = "Total Likes",
                            value = "%,d".format(state.totalLikes),
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticsStatCard(
                            label = "Total Comments",
                            value = "%,d".format(state.totalComments),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    AnalyticsTrendChart(trendValues = state.weeklyTrendValues)
                }

                item {
                    TopRecipesSection(topRecipes = state.topRecipes)
                }
            }
        }
    }
}