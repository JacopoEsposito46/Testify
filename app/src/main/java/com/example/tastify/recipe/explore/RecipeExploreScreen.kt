package com.example.tastify.recipe.explore

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tastify.components.SharedBottomNavBar
import com.example.tastify.models.CuisineType
import com.example.tastify.models.DietaryRestriction
import com.example.tastify.models.ExploreCategory
import com.example.tastify.models.Recipe
import com.example.tastify.models.RecipeDifficulty
import com.example.tastify.recipe.explore.components.CategoryCard
import com.example.tastify.recipe.explore.components.ExploreRecipeCard
import com.example.tastify.recipe.explore.components.ExploreSearchBar
import com.example.tastify.recipe.explore.components.SearchResultCard
import com.example.tastify.recipe.explore.logic.RecipeExploreEvent
import com.example.tastify.recipe.explore.logic.RecipeExploreState
import com.example.tastify.recipe.explore.logic.RecipeExploreViewModel

private data class CategoryItem(val title: String, val imageUrl: String, val type: ExploreCategory)
private val exploreCategoriesList = listOf(
    CategoryItem("Vegetarian", "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=500", ExploreCategory.VEGETARIAN),
    CategoryItem("Gluten-Free", "https://images.unsplash.com/photo-1504630083234-14187a9df0f5?w=500", ExploreCategory.GLUTEN_FREE),
    CategoryItem("Easy to Make", "https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=500", ExploreCategory.EASY),
    CategoryItem("Japanese Cuisine", "https://images.unsplash.com/photo-1580822184713-fc5400e7fe10?w=500", ExploreCategory.JAPANESE)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeExploreScreen(
    viewModel: RecipeExploreViewModel,
    onRecipeClick: (Recipe) -> Unit,
    onToggleFavourite: (String) -> Unit,
    onNavigateToRecipeProposalList: () -> Unit,
    onNavigateToMyProfile: () -> Unit,
    onNavigateToFilters: () -> Unit,
    onNavigateToCategory: (ExploreCategory) -> Unit,
    onNavigateToCookBook: () -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onAddRecipe: () -> Unit = {},
    onNavigateToImportRecipe: () -> Unit = {},
    unreadNotificationCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentQuery by viewModel.currentSearchQuery.collectAsStateWithLifecycle()
    var showAddRecipeSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val hasActiveFilters = state.selectedDifficulty != null ||
            state.servings > 1 ||
            state.costRange.start > 1f || state.costRange.endInclusive < 5f ||
            state.maxCalories < 5000f ||
            state.selectedDietaryRestrictions.isNotEmpty() ||
            state.selectedCuisineTypes.isNotEmpty() ||
            state.includedIngredients.isNotEmpty()

    val isSearching = currentQuery.isNotEmpty() || hasActiveFilters

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddRecipeSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new recipe"
                )
            }
        },
        bottomBar = {
            SharedBottomNavBar(
                selectedRoute = "home",
                onRouteSelected = { route ->
                    when (route) {
                        "home" -> onNavigateToRecipeProposalList()
                        "profile" -> onNavigateToMyProfile()
                        "cookbook" -> onNavigateToCookBook()
                        "updates" -> onNavigateToNotifications()
                        "chat" -> onNavigateToChat()
                        else -> {}
                    }
                },
                unreadNotificationCount = unreadNotificationCount
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (!isSearching) {
                Spacer(modifier = Modifier.height(8.dp))
            }

            ExploreSearchBar(
                query = currentQuery,
                onQueryChange = { viewModel.onEvent(RecipeExploreEvent.UpdateSearchQuery(it)) },
                onFilterClick = onNavigateToFilters
            )

            // Progress indicator during filtering
            if (state.isFiltering) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.secondary
                )
            } else {
                Spacer(modifier = Modifier.height(2.dp))
            }

            QuickFilterChips(
                state = state,
                onEvent = { viewModel.onEvent(it) }
            )

            AnimatedContent(
                targetState = isSearching,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "SearchTransition"
            ) { searching ->
                if (!searching) {
                    // Default Discovery View originale
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        item {
                            SectionHeader(title = "Most Popular Today", onExploreClick = { onNavigateToCategory(ExploreCategory.POPULAR) })
                        }

                        val popularChunks = state.recipes.sortedByDescending { it.rating }.take(10).chunked(2)
                        items(
                            items = popularChunks,
                            key = { chunk -> "popular_${chunk.firstOrNull()?.recipeId ?: 0}" }
                        ) { rowRecipes ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowRecipes.forEach { recipe ->
                                    ExploreRecipeCard(
                                        recipe = recipe,
                                        onClick = { onRecipeClick(recipe) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowRecipes.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            SectionHeader(title = "Italian Cuisine", onExploreClick = { onNavigateToCategory(ExploreCategory.ITALIAN) })
                        }

                        val italianRecipes = state.recipes.filter { it.cuisineType == CuisineType.ITALIAN }
                        val italianChunks = italianRecipes.take(6).chunked(2)
                        items(
                            items = italianChunks,
                            key = { chunk -> "italian_${chunk.firstOrNull()?.recipeId ?: 0}" }
                        ) { rowRecipes ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                rowRecipes.forEach { recipe ->
                                    ExploreRecipeCard(
                                        recipe = recipe,
                                        onClick = { onRecipeClick(recipe) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowRecipes.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            SectionHeader(title = "Categories", onExploreClick = null)

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    CategoryCard(title = exploreCategoriesList[0].title, imageUrl = exploreCategoriesList[0].imageUrl, onClick = { onNavigateToCategory(exploreCategoriesList[0].type) }, modifier = Modifier.weight(1f))
                                    CategoryCard(title = exploreCategoriesList[1].title, imageUrl = exploreCategoriesList[1].imageUrl, onClick = { onNavigateToCategory(exploreCategoriesList[1].type) }, modifier = Modifier.weight(1f))
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    CategoryCard(title = exploreCategoriesList[2].title, imageUrl = exploreCategoriesList[2].imageUrl, onClick = { onNavigateToCategory(exploreCategoriesList[2].type) }, modifier = Modifier.weight(1f))
                                    CategoryCard(title = exploreCategoriesList[3].title, imageUrl = exploreCategoriesList[3].imageUrl, onClick = { onNavigateToCategory(exploreCategoriesList[3].type) }, modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                } else {
                    // Search Results View originale ripristinata con adattamento dati preferiti
                    if (state.recipes.isEmpty()) {
                        EmptySearchState(
                            onResetFilters = { viewModel.onEvent(RecipeExploreEvent.ResetFilters) }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = state.recipes,
                                key = { it.recipeId }
                            ) { recipe ->
                                SearchResultCard(
                                    recipe = recipe,
                                    isFavorite = state.favoriteRecipeIds.contains(recipe.recipeId),
                                    onClick = { onRecipeClick(recipe) },
                                    onRemoveClick = null,
                                    onFavouriteClick = { viewModel.onEvent(RecipeExploreEvent.ToggleFavourite(recipe.recipeId)) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddRecipeSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddRecipeSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("Create Manually", fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Create manually your own recipe") },
                        leadingContent = {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            showAddRecipeSheet = false
                            onAddRecipe()
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Import from Social", fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Import a recipe from social or URL") },
                        leadingContent = {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            showAddRecipeSheet = false
                            onNavigateToImportRecipe()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuickFilterChips(
    state: RecipeExploreState,
    onEvent: (RecipeExploreEvent) -> Unit
) {

    val filters = listOf(
        "All" to null,
        "Vegetarian" to DietaryRestriction.VEGETARIAN,
        "Vegan" to DietaryRestriction.VEGAN,
        "Gluten-Free" to DietaryRestriction.GLUTEN_FREE,
        "Dairy-Free" to DietaryRestriction.DAIRY_FREE,
        "Italian" to CuisineType.ITALIAN,
        "Japanese" to CuisineType.JAPANESE,
        "Mexican" to CuisineType.MEXICAN,
        "French" to CuisineType.FRENCH,
        "Easy" to RecipeDifficulty.EASY,
        "Medium" to RecipeDifficulty.MEDIUM,
        "Hard" to RecipeDifficulty.HARD
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters) { (label, value) ->
            val isSelected = when (value) {
                null -> state.selectedDietaryRestrictions.isEmpty() && state.selectedCuisineTypes.isEmpty() && state.selectedDifficulty == null
                is DietaryRestriction -> state.selectedDietaryRestrictions.contains(value)
                is CuisineType -> state.selectedCuisineTypes.contains(value)
                is RecipeDifficulty -> state.selectedDifficulty == value
                else -> false
            }

            FilterChip(
                selected = isSelected,
                onClick = {
                    when (value) {
                        null -> onEvent(RecipeExploreEvent.ResetFilters)
                        is DietaryRestriction -> onEvent(
                            RecipeExploreEvent.ToggleDietaryRestriction(
                                value
                            )
                        )

                        is CuisineType -> onEvent(RecipeExploreEvent.ToggleCuisineType(value))
                        is RecipeDifficulty -> onEvent(RecipeExploreEvent.UpdateDifficulty(if (isSelected) null else value))
                    }
                },
                label = { Text(label) },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color.LightGray,
                    selectedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, onExploreClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
        if (onExploreClick != null) {
            TextButton(
                onClick = onExploreClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Explore",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptySearchState(onResetFilters: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No recipes found",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Try adjusting your filters or search query to find what you're looking for.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onResetFilters,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Clear all filters", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}