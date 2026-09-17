package com.example.tastify.recipe.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tastify.components.SharedTopBar
import com.example.tastify.models.Recipe
import com.example.tastify.recipe.explore.components.ExploreRecipeCard
import com.example.tastify.recipe.explore.logic.CategoryGridViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryGridScreen(
    viewModel: CategoryGridViewModel,
    onRecipeClick: (Recipe) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SharedTopBar(
                title = state.categoryTitle,
                onBack = onNavigateBack,
                actions = {}
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = state.recipes,
                    key = { it.recipeId }
                ) { recipe ->
                    ExploreRecipeCard(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
