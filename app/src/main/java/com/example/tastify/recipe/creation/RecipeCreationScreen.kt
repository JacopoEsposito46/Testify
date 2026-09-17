package com.example.tastify.recipe.creation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tastify.components.SharedTopBar
import com.example.tastify.recipe.creation.logic.RecipeCreationEvent
import com.example.tastify.recipe.creation.logic.RecipeCreationViewModel
import com.example.tastify.recipe.creation.components.ImagePicker
import com.example.tastify.recipe.creation.components.RecipeCreationBottomBar
import com.example.tastify.recipe.creation.components.RecipeCreationIngredients
import com.example.tastify.recipe.creation.components.RecipeCreationStats
import com.example.tastify.recipe.creation.components.RecipeCreationSteps
import com.example.tastify.recipe.creation.components.RecipeCreationTags

@Suppress("DEPRECATION")
@Composable
fun RecipeCreationScreen(
    viewModel: RecipeCreationViewModel,
    isEditing: Boolean = false,
    isCopying: Boolean = false,
    onNavigateBack: () -> Unit,
    onPublishSuccess: () -> Unit
){
    val recipeState by viewModel.uiState.collectAsStateWithLifecycle()
    val newRecipe = recipeState.draftRecipe

    LaunchedEffect(recipeState.saveSuccess) {
        if (recipeState.saveSuccess) {
            onPublishSuccess()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SharedTopBar(
                title = if (isEditing) "Edit Recipe" else "Create Recipe",
                onBack = onNavigateBack,
                actions = {}
                )},
        bottomBar = {
            RecipeCreationBottomBar(
                isEditing = isEditing,
                onPublishClick = { viewModel.onEvent(RecipeCreationEvent.SaveRecipe(isEditing, isCopying)) },
                onSavePrivateClick = { viewModel.onEvent(RecipeCreationEvent.SaveRecipe(isEditing, isCopying, isPrivate = true)) },
                onDeleteClick = { 
                    viewModel.onEvent(RecipeCreationEvent.ResetDraftRecipe(newRecipe.authorId)) 
                    onNavigateBack()
                }
            )
        }
    ){ innerPadding ->
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            //Image cover
            ImagePicker(
                imageUri = newRecipe.recipePhotoId,
                true,
                Modifier
                    .fillMaxWidth()
                    .height(if (newRecipe.recipePhotoId.isEmpty()) 150.dp else 300.dp),
                isError = recipeState.coverImageError != null,
                errorMessage = recipeState.coverImageError,
                onImageSelected = { newUri ->
                    viewModel.onEvent(RecipeCreationEvent.UpdateDraftRecipe(newRecipe.copy(recipePhotoId = newUri)))
                    viewModel.onEvent(RecipeCreationEvent.ClearCoverImageError)
                }
            )

            if (isCopying) {
                recipeState.copyError?.let { copyErr ->
                    Text(
                        text = copyErr,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            //Title
            OutlinedTextField(
                value = newRecipe.title,
                onValueChange = {
                    viewModel.onEvent(RecipeCreationEvent.UpdateDraftRecipe(newRecipe.copy(title = it)))
                    viewModel.onEvent(RecipeCreationEvent.ClearTitleError)
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                isError = recipeState.titleError != null,
                supportingText = {
                    recipeState.titleError?.let { titleErr ->
                        Text(
                            text = titleErr,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                trailingIcon = {
                    if (newRecipe.title.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.onEvent(RecipeCreationEvent.UpdateDraftRecipe(newRecipe.copy(title = "")))
                        }){
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text = "Recipe name",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )

            //Description
            OutlinedTextField(
                value = newRecipe.description,
                onValueChange = {
                    viewModel.onEvent(RecipeCreationEvent.UpdateDraftRecipe(newRecipe.copy(description = it)))
                    viewModel.onEvent(RecipeCreationEvent.ClearDescriptionError)
                    },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                isError = recipeState.descriptionError != null,
                supportingText = {
                    recipeState.descriptionError?.let { descErr ->
                        Text(
                            text = descErr,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                trailingIcon = {
                    if (newRecipe.description.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.onEvent(RecipeCreationEvent.UpdateDraftRecipe(newRecipe.copy(description = "")))
                        }){
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text = "Recipe Description",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            )

            //Stats
            RecipeCreationStats(
                recipe = newRecipe,
                onValueChange = { viewModel.onEvent(RecipeCreationEvent.UpdateDraftRecipe(it)) },
                timeError = recipeState.timeError,
                caloriesError = recipeState.caloriesError,
                servesError = recipeState.servesError,
                onClearTimeError = { viewModel.onEvent(RecipeCreationEvent.ClearTimeError) },
                onClearCaloriesError = { viewModel.onEvent(RecipeCreationEvent.ClearCaloriesError) },
                onClearServesError = { viewModel.onEvent(RecipeCreationEvent.ClearServesError) },
            )

            //Ingredients
            RecipeCreationIngredients(
                recipe = newRecipe,
                ingredientCatalog = recipeState.ingredientCatalog,
                onValueChange = { viewModel.onEvent(RecipeCreationEvent.UpdateDraftRecipe(it)) },
                ingredientsError = recipeState.ingredientsError,
                ingredientNameErrors = recipeState.ingredientNameErrors,
                ingredientQuantityErrors = recipeState.ingredientQuantityErrors,
                ingredientUnitErrors = recipeState.ingredientUnitErrors,
                onClearIngredientsError = { viewModel.onEvent(RecipeCreationEvent.ClearIngredientsError) },
                onClearIngredientNameError = { index -> viewModel.onEvent(RecipeCreationEvent.ClearIngredientNameError(index)) },
                onClearIngredientQuantityError = { index -> viewModel.onEvent(RecipeCreationEvent.ClearIngredientQuantityError(index)) },
                onClearIngredientUnitError = { index -> viewModel.onEvent(RecipeCreationEvent.ClearIngredientUnitError(index)) }
            )

            //Cuisinary and dietary restriction tags
            RecipeCreationTags(
                recipe = newRecipe,
                onValueChange = { viewModel.onEvent(RecipeCreationEvent.UpdateDraftRecipe(it)) }
            )

            //Steps
            RecipeCreationSteps(
                recipe = newRecipe,
                onValueChange = { viewModel.onEvent(RecipeCreationEvent.UpdateDraftRecipe(it)) },
                stepsError = recipeState.stepsError,
                stepImageErrors = recipeState.stepImageErrors,
                stepTitleErrors = recipeState.stepTitleErrors,
                stepDescriptionErrors = recipeState.stepDescriptionErrors,
                onClearStepsError = { viewModel.onEvent(RecipeCreationEvent.ClearStepsError) },
                onClearStepImageError = { index -> viewModel.onEvent(RecipeCreationEvent.ClearStepImageError(index)) },
                onClearStepTitleError = { index -> viewModel.onEvent(RecipeCreationEvent.ClearStepTitleError(index)) },
                onClearStepDescriptionError = { index -> viewModel.onEvent(RecipeCreationEvent.ClearStepDescriptionError(index)) }
            )
        }
    }
}