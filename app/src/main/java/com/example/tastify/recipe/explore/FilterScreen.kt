package com.example.tastify.recipe.explore

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tastify.components.SharedTopBar
import com.example.tastify.models.CuisineType
import com.example.tastify.models.DietaryRestriction
import com.example.tastify.models.RecipeDifficulty
import com.example.tastify.recipe.explore.logic.RecipeExploreEvent
import com.example.tastify.recipe.explore.logic.RecipeExploreViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    viewModel: RecipeExploreViewModel,
    onBack: () -> Unit,
    onApply: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var ingredientInput by remember { mutableStateOf("") }

    fun submitIngredientInput() {
        val cleanIngredient = ingredientInput.trim()
        if (cleanIngredient.isNotEmpty()) {
            viewModel.onEvent(RecipeExploreEvent.AddIngredient(cleanIngredient))
            ingredientInput = ""
        }
    }

    Scaffold(
        topBar = {
            SharedTopBar(
                title = "Filters",
                onBack = onBack,
                actions = {}
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.onEvent(RecipeExploreEvent.ResetFilters) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Text("Reset", color = Color.Gray)
                    }
                    Button(
                        onClick = {
                            submitIngredientInput()
                            onApply()
                        },
                        modifier = Modifier.weight(2f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Apply Filters", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Difficulty Section
            FilterSection(title = "Difficulty") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RecipeDifficulty.entries.forEach { difficulty ->
                        val isSelected = state.selectedDifficulty == difficulty
                        FilterButton(
                            text = difficulty.displayName,
                            isSelected = isSelected,
                            onClick = { viewModel.onEvent(RecipeExploreEvent.UpdateDifficulty(if (isSelected) null else difficulty)) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Servings Section
            FilterSection(
                title = "Servings",
                badge = "${state.servings} People"
            ) {
                Slider(
                    value = state.servings.toFloat(),
                    onValueChange = { viewModel.onEvent(RecipeExploreEvent.UpdateServings(it.toInt())) },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.secondary
                    )
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("1", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("10+", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            // Cost Range Section
            FilterSection(
                title = "Cost Range",
                badge = "${"$".repeat(state.costRange.start.toInt())} — ${"$".repeat(state.costRange.endInclusive.toInt())}"
            ) {
                RangeSlider(
                    value = state.costRange,
                    onValueChange = { viewModel.onEvent(RecipeExploreEvent.UpdateCostRange(it)) },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.secondary
                    )
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("$", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("$$$$$", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            // Calories Section
            FilterSection(
                title = "Calories",
                badge = if (state.maxCalories >= 5000f) "Any" else "Under ${state.maxCalories.toInt()} kcal"
            ) {
                Slider(
                    value = state.maxCalories,
                    onValueChange = { viewModel.onEvent(RecipeExploreEvent.UpdateMaxCalories(it)) },
                    valueRange = 0f..5000f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.secondary
                    )
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("0 kcal", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("5000+ kcal", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            // Dietary Restrictions
            var isDietaryExpanded by remember { mutableStateOf(false) }
            val visibleDietary = DietaryRestriction.entries
            
            FilterSection(
                title = "Dietary Restrictions",
                actionText = if (isDietaryExpanded) "Show less" else "Show all options",
                onActionClick = { isDietaryExpanded = !isDietaryExpanded }
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxLines = if (isDietaryExpanded) Int.MAX_VALUE else 1,
                    modifier = Modifier.animateContentSize()
                ) {
                    visibleDietary.forEach { restriction ->
                        val isSelected = state.selectedDietaryRestrictions.contains(restriction)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onEvent(RecipeExploreEvent.ToggleDietaryRestriction(restriction)) },
                            label = { Text(restriction.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
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

            // Cuisine Type
            var isCuisineExpanded by remember { mutableStateOf(false) }
            val visibleCuisine = CuisineType.entries

            FilterSection(
                title = "Cuisine Type",
                actionText = if (isCuisineExpanded) "Show less" else "Show all options",
                onActionClick = { isCuisineExpanded = !isCuisineExpanded }
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxLines = if (isCuisineExpanded) Int.MAX_VALUE else 1,
                    modifier = Modifier.animateContentSize()
                ) {
                    visibleCuisine.forEach { cuisine ->
                        val isSelected = state.selectedCuisineTypes.contains(cuisine)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onEvent(RecipeExploreEvent.ToggleCuisineType(cuisine)) },
                            label = { Text(cuisine.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            ),
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

            // Include Ingredients
            FilterSection(title = "Include Ingredients") {
                OutlinedTextField(
                    value = ingredientInput,
                    onValueChange = { ingredientInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Add ingredient...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            submitIngredientInput()
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    trailingIcon = {
                        if (ingredientInput.isNotBlank()) {
                            IconButton(onClick = {
                                submitIngredientInput()
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add ingredient")
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.includedIngredients.forEach { ingredient ->
                        IngredientTag(
                            text = ingredient,
                            onRemove = { viewModel.onEvent(RecipeExploreEvent.RemoveIngredient(ingredient)) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FilterSection(
    title: String,
    badge: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            if (badge != null) {
                Surface(
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        badge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        content()
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { onActionClick() },
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun FilterButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
        )
    ) {
        Text(text, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
    }
}

@Composable
fun IngredientTag(text: String, onRemove: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onRemove() },
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
