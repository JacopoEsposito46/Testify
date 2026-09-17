package com.example.tastify.recipe.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tastify.models.Recipe
import com.example.tastify.components.SharedTopBar
import com.example.tastify.recipe.explore.components.ExploreSearchBar
import com.example.tastify.recipe.explore.components.SearchResultCard

@Composable
fun RecipeList(
    recipes: List<Recipe>,
    modifier: Modifier = Modifier,
    favoriteRecipeIds: List<String> = emptyList(),
    onBack: () -> Unit,
    onRecipeClick: (Recipe) -> Unit,
    onFilterClick: (() -> Unit)? = null,
    onRemoveRecipe: ((Recipe) -> Unit)? = null,
    onToggleFavourite: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredRecipes = if (searchQuery.isBlank()) {
        recipes
    } else {
        recipes.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SharedTopBar(
                title = "Explore",
                onBack = onBack,
                actions = {}
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            ExploreSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onFilterClick = onFilterClick
            )

            if (filteredRecipes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recipe found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = filteredRecipes,
                        key = { it.recipeId }
                    ) { recipe ->
                        SearchResultCard(
                            recipe = recipe,
                            isFavorite = favoriteRecipeIds.contains(recipe.recipeId),
                            onClick = { onRecipeClick(recipe) },
                            onRemoveClick = if (onRemoveRecipe != null) { { onRemoveRecipe(recipe) } } else null,
                            onFavouriteClick = { onToggleFavourite(recipe.recipeId) }
                        )
                    }
                }
            }
        }
    }
}