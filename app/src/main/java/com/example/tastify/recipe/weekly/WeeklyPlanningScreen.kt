package com.example.tastify.recipe.weekly

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tastify.components.SharedTopBar
import com.example.tastify.recipe.weekly.components.DaySelectorRow
import com.example.tastify.recipe.weekly.components.MealSection
import com.example.tastify.recipe.weekly.components.RecipeSelectionBottomSheet
import com.example.tastify.recipe.weekly.logic.WeeklyPlanningUiState
import com.example.tastify.recipe.weekly.logic.WeeklyPlanningViewModel

@Composable
fun WeeklyPlanningScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRecipe: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WeeklyPlanningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WeeklyPlanningScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onDaySelected = viewModel::selectDay,
        onAddRecipeClick = viewModel::openAddRecipeBottomSheet,
        onRemoveRecipe = viewModel::removeRecipeFromSlot,
        onRecipeClick = onNavigateToRecipe,
        modifier = modifier
    )

    if (uiState.isBottomSheetOpen) {
        RecipeSelectionBottomSheet(
            courses = uiState.customCourses,
            displayedRecipes = uiState.displayedRecipes,
            selectedFilter = uiState.selectedFilter,
            onSearchQueryChange = viewModel::setSearchQuery,
            onFilterSelected = viewModel::setSelectedFilter,
            onDismiss = viewModel::closeBottomSheet,
            onRecipeSelect = viewModel::addRecipeToSlot
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeeklyPlanningScreenContent(
    uiState: WeeklyPlanningUiState,
    onNavigateBack: () -> Unit,
    onDaySelected: (String) -> Unit,
    onAddRecipeClick: (String) -> Unit,
    onRemoveRecipe: (String, String) -> Unit,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SharedTopBar(
                title = "Weekly Planning",
                onBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                DaySelectorRow(
                    days = uiState.days,
                    onDaySelected = onDaySelected
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            items(uiState.mealSlots) { slot ->
                MealSection(
                    mealSlot = slot,
                    onAddRecipe = onAddRecipeClick,
                    onRemoveRecipe = onRemoveRecipe,
                    onRecipeClick = onRecipeClick
                )
            }
        }
    }
}
