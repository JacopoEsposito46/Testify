package com.example.tastify.recipe.weekly.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tastify.models.CustomCourse
import com.example.tastify.models.Recipe
import com.example.tastify.recipe.explore.components.ExploreRecipeCard
import com.example.tastify.ui.theme.PageTitle
import com.example.tastify.ui.theme.SubTitle
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeSelectionBottomSheet(
    courses: List<CustomCourse>,
    displayedRecipes: List<Recipe>,
    selectedFilter: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onRecipeSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }

    LaunchedEffect(textInput) {
        delay(300.milliseconds) // debounce
        onSearchQueryChange(textInput)
    }

    val allFilters = remember(courses) {
        listOf("All", "Favorites", "My Recipes", "Imported") + courses.map { it.title }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.9f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Select Recipe",
                style = MaterialTheme.typography.headlineSmall,
                color = PageTitle,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(24.dp), clip = false),
                placeholder = { 
                    Text(
                        text = "Search recipes...", 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Search, 
                        contentDescription = "Search", 
                        tint = MaterialTheme.colorScheme.primary
                    ) 
                },
                trailingIcon = {
                    if (textInput.isNotEmpty()) {
                        IconButton(onClick = { textInput = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice search",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(allFilters) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter && textInput.isEmpty(),
                        onClick = {
                            onFilterSelected(filter)
                            textInput = "" // clear search when switching tabs explicitly
                            onSearchQueryChange("")
                        },
                        label = { 
                            Text(
                                text = filter,
                                fontWeight = if (selectedFilter == filter && textInput.isEmpty()) FontWeight.Bold else FontWeight.Medium
                            ) 
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (displayedRecipes.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp).padding(bottom = 16.dp),
                        tint = Color.LightGray
                    )
                    Text(
                        text = "No recipes found",
                        style = MaterialTheme.typography.titleMedium,
                        color = PageTitle,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Try a different search or list",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SubTitle,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedRecipes, key = { it.recipeId }) { recipe ->
                        ExploreRecipeCard(
                            recipe = recipe,
                            onClick = { 
                                onRecipeSelect(recipe.recipeId)
                                coroutineScope.launch {
                                    try {
                                        sheetState.hide()
                                    } finally {
                                        onDismiss()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
