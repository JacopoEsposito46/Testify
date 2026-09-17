package com.example.tastify.recipe.creation.logic

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.tastify.navigation.CopyRecipeProposalRoute
import com.example.tastify.navigation.CreateNewRecipeProposalRoute
import com.example.tastify.navigation.EditRecipeProposalRoute
import com.example.tastify.domain.GetRecipeByIdUseCase
import com.example.tastify.domain.AddRecipeUseCase
import com.example.tastify.domain.UpdateRecipeUseCase
import com.example.tastify.domain.NotificationUseCases
import com.example.tastify.domain.GetUserProfileUseCase
import com.example.tastify.domain.GetAllIngredientsUseCase
import com.example.tastify.domain.GetOrCreateCustomIngredientUseCase
import com.example.tastify.models.Ingredient
import com.example.tastify.models.Recipe
import com.example.tastify.models.RecipeIngredient
import com.example.tastify.models.RecipeStep
import com.example.tastify.notifications.SnackbarManager
import com.example.tastify.models.ExtractedRecipeData
import com.example.tastify.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class RecipeCreationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getRecipeByIdUseCase: GetRecipeByIdUseCase,
    private val addRecipeUseCase: AddRecipeUseCase,
    private val updateRecipeUseCase: UpdateRecipeUseCase,
    private val notificationUseCases: NotificationUseCases,
    private val getAllIngredientsUseCase: GetAllIngredientsUseCase,
    private val getOrCreateCustomIngredientUseCase: GetOrCreateCustomIngredientUseCase,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeCreationState())
    val uiState: StateFlow<RecipeCreationState> = _uiState.asStateFlow()
    private val currentUserId = SessionManager.CURRENT_LOGGED_IN_USER_ID ?: ""

    init {
        viewModelScope.launch {
            getAllIngredientsUseCase().collect { catalog ->
                _uiState.update { it.copy(ingredientCatalog = catalog) }
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Wait for nickname to be available
            val nickname = getUserProfileUseCase(currentUserId).first().let { profile ->
                profile.username.ifBlank { profile.name }
            }
            // Edit or Copy or Create New
            try {
                val editRoute = savedStateHandle.toRoute<EditRecipeProposalRoute>()
                val recipeId = editRoute.editRecipeId
                loadRecipeForEdit(recipeId)
            } catch (e: IllegalArgumentException) {
                try {
                    val copyRoute = savedStateHandle.toRoute<CopyRecipeProposalRoute>()
                    val recipeId = copyRoute.copyRecipeId
                    loadRecipeForCopy(recipeId, nickname)
                } catch (e2: IllegalArgumentException) {
                    try {
                        val createRoute = savedStateHandle.toRoute<CreateNewRecipeProposalRoute>()
                        val importedDataJson = createRoute.importedDataJson
                        if (importedDataJson != null) {
                            val extractedData = Json.decodeFromString<ExtractedRecipeData>(importedDataJson)
                            loadRecipeFromImportedData(extractedData, nickname)
                        } else {
                            loadEmptyRecipe(nickname)
                        }
                    } catch (e3: IllegalArgumentException) {
                        loadEmptyRecipe(nickname)
                    }
                }
            }
        }
    }

    private fun loadEmptyRecipe(nickname: String) {
        _uiState.update {
            it.copy(
                draftRecipe = Recipe(
                    authorId = currentUserId,
                    authorNickname = nickname,
                    ingredients = listOf(RecipeIngredient(), RecipeIngredient()),
                    steps = listOf(RecipeStep(stepNumber = 1), RecipeStep(stepNumber = 2))
                ),
                isLoading = false
            )
        }
    }

    private fun loadRecipeFromImportedData(extractedData: ExtractedRecipeData, nickname: String) {
        val initialIngredients = extractedData.ingredients.map { ingredientStr ->
            val parsed = parseIngredientString(ingredientStr)
            RecipeIngredient(
                ingredient = Ingredient(name = parsed.name),
                quantity = parsed.quantity,
                unit = parsed.unit
            )
        }.ifEmpty { listOf(RecipeIngredient(), RecipeIngredient()) }

        val initialSteps = extractedData.instructions.mapIndexed { index, stepStr ->
            RecipeStep(
                stepNumber = index + 1,
                title = "Step ${index + 1}",
                description = stepStr
            )
        }.ifEmpty { listOf(RecipeStep(stepNumber = 1), RecipeStep(stepNumber = 2)) }

        _uiState.update {
            it.copy(
                draftRecipe = Recipe(
                    authorId = currentUserId,
                    authorNickname = nickname,
                    source = Recipe.SOURCE_IMPORTED,
                    title = extractedData.title ?: "",
                    description = extractedData.description ?: "",
                    recipePhotoId = extractedData.imageUrl ?: "",
                    cookTime = extractedData.cookTimeMinutes ?: extractedData.prepTimeMinutes ?: 0,
                    calories = extractedData.calories ?: 0,
                    serves = extractedData.serves ?: 1,
                    ingredients = initialIngredients,
                    steps = initialSteps
                ),
                isLoading = false
            )
        }
        snackbarManager.showMessage("Recipe data imported successfully")
    }

    private data class ParsedIngredient(val quantity: Double, val unit: String, val name: String)

    private fun parseIngredientString(rawString: String): ParsedIngredient {
        var str = rawString.trim()
        var quantity = 1.0
        var unit = "pz"

        // Lista di unità
        val units = listOf(
            "g", "kg", "mg", "ml", "l", "dl", "cucchiai", "cucchiaio",
            "cucchiaini", "cucchiaino", "tazza", "tazze", "spicchio",
            "spicchi", "pizzico", "mazzetto", "fetta", "fette", "pz"
        )
        val sortedUnits = units.sortedByDescending { it.length }

        // Cerca il pattern "Numero + Unità"
        val qtyUnitRegex = Regex("\\b([0-9]+(?:[.,][0-9]+)?(?:\\s*/\\s*[0-9]+)?)\\s*(${sortedUnits.joinToString("|")})\\b", RegexOption.IGNORE_CASE)
        val matchQtyUnit = qtyUnitRegex.find(str)

        if (matchQtyUnit != null) {
            val qtyStr = matchQtyUnit.groupValues[1].replace(",", ".")
            quantity = parseFractionOrDouble(qtyStr)
            unit = matchQtyUnit.groupValues[2].lowercase()
            str = str.replaceRange(matchQtyUnit.range, "").trim()
        } else {
            // Se non c'è unità, cerca solo un numero
            val qtyRegex = Regex("\\b([0-9]+(?:[.,][0-9]+)?(?:\\s*/\\s*[0-9]+)?)\\b")
            val matchQty = qtyRegex.find(str)
            if (matchQty != null) {
                val qtyStr = matchQty.groupValues[1].replace(",", ".")
                quantity = parseFractionOrDouble(qtyStr)
                str = str.replaceRange(matchQty.range, "").trim()
            } else {
                // Controllo per q.b.
                val qbRegex = Regex("\\b(q\\.b\\.?|qb)\\b", RegexOption.IGNORE_CASE)
                val matchQb = qbRegex.find(str)
                if (matchQb != null) {
                    unit = "q.b."
                    quantity = 0.0
                    str = str.replaceRange(matchQb.range, "").trim()
                }
            }
        }

        str = str.replace(Regex("^(di|d'|circa)\\s+", RegexOption.IGNORE_CASE), "")
        str = str.replace(Regex("\\s+(di|d'|circa)$", RegexOption.IGNORE_CASE), "")
        str = str.replace(Regex("^[,;\\-]\\s*"), "")
        str = str.replace(Regex("\\s*[,;\\-]$"), "")
        str = str.trim()

        var name = str.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        if (name.isBlank()) name = "Unknown Ingredient"

        quantity = Math.round(quantity * 100.0) / 100.0

        return ParsedIngredient(quantity, unit, name)
    }

    private fun parseFractionOrDouble(str: String): Double {
        if (str.contains("/")) {
            val parts = str.split("/")
            if (parts.size == 2) {
                val num = parts[0].trim().toDoubleOrNull() ?: 0.0
                val den = parts[1].trim().toDoubleOrNull() ?: 1.0
                if (den != 0.0) return num / den
            }
        }
        return str.toDoubleOrNull() ?: 1.0
    }

    private suspend fun loadRecipeForEdit(recipeId: String) {
        val recipe = getRecipeByIdUseCase(recipeId).first()
        if (recipe != null) {
            _uiState.update { it.copy(draftRecipe = recipe, isLoading = false) }
        } else {
            _uiState.update { it.copy(isLoading = false) }
            snackbarManager.showMessage("Recipe not found")
        }
    }

    private suspend fun loadRecipeForCopy(recipeId: String, nickname: String) {
        val recipe = getRecipeByIdUseCase(recipeId).first()
        if (recipe != null) {
            val copiedRecipe = recipe.copy(
                recipeId = UUID.randomUUID().toString(),
                authorId = currentUserId,
                authorNickname = nickname,
                views = 0,
                favouriteCount = 0,
                commentCount = 0,
                reviewCount = 0,
                rating = 0.0
            )
            _uiState.update { it.copy(
                draftRecipe = copiedRecipe,
                originalRecipeForCopy = recipe,
                isLoading = false
            )}
        } else {
            _uiState.update { it.copy(isLoading = false) }
            snackbarManager.showMessage("Recipe not found")
        }
    }

    fun onEvent(event: RecipeCreationEvent) {
        when (event) {
            is RecipeCreationEvent.UpdateDraftRecipe -> {
                _uiState.update { it.copy(draftRecipe = event.recipe) }
            }
            is RecipeCreationEvent.ResetDraftRecipe -> {
                _uiState.update {
                    it.copy(
                        draftRecipe = Recipe(
                            authorId = event.authorId,
                            ingredients = listOf(RecipeIngredient(), RecipeIngredient()),
                            steps = listOf(RecipeStep(stepNumber = 1), RecipeStep(stepNumber = 2))
                        ),
                        originalRecipeForCopy = null,
                        copyError = null,
                        saveSuccess = false
                    )
                }
                clearAllValidationErrors()
            }
            is RecipeCreationEvent.SaveRecipe -> {
                val recipe = _uiState.value.draftRecipe
                if (validateRecipe(recipe, event.isCopying)) {
                    viewModelScope.launch(Dispatchers.IO) {
                        _uiState.update { it.copy(isLoading = true) }

                        try {
                            val processedIngredients = recipe.ingredients.map { recIng ->
                                val resolvedIngredient = getOrCreateCustomIngredientUseCase(recIng.ingredient.name)
                                recIng.copy(ingredient = resolvedIngredient)
                            }

                            val recipeToSave = recipe.copy(ingredients = processedIngredients)
                            val finalRecipe = when {
                                event.isEditing -> recipeToSave.copy(
                                    isPublic = recipeToSave.isPublic && recipeToSave.reportCount < 2
                                )
                                event.isPrivate -> recipeToSave.copy(isPublic = false)
                                else -> recipeToSave.copy(isPublic = true)
                            }

                            if (event.isEditing) {
                                updateRecipeUseCase(finalRecipe)
                            } else {
                                addRecipeUseCase(finalRecipe)
                                val originalRecipe = _uiState.value.originalRecipeForCopy
                                if (originalRecipe != null) {
                                    notificationUseCases.notifyDuplication(
                                        originalAuthorId = originalRecipe.authorId,
                                        forkerName = finalRecipe.authorNickname,
                                        recipeTitle = originalRecipe.title,
                                        recipeId = finalRecipe.recipeId
                                    )
                                }
                            }
                            _uiState.update { it.copy(saveSuccess = true, isLoading = false) }
                            withContext(Dispatchers.Main) {
                                snackbarManager.showMessage(
                                    when {
                                        event.isEditing -> "Recipe updated successfully"
                                        event.isPrivate -> "Recipe saved to your profile"
                                        else -> "Recipe published successfully"
                                    }
                                )
                            }
                        } catch (e: Exception) {
                            Log.e(e.toString(), "Recipe save failed")
                            _uiState.update { it.copy(isLoading = false) }
                            withContext(Dispatchers.Main) {
                                snackbarManager.showMessage("Recipe image upload failed. Check Supabase storage policies.")
                            }
                        }
                    }
                }
            }

            // Clear error events
            is RecipeCreationEvent.ClearCoverImageError -> _uiState.update { it.copy(coverImageError = null) }
            is RecipeCreationEvent.ClearTitleError -> _uiState.update { it.copy(titleError = null) }
            is RecipeCreationEvent.ClearDescriptionError -> _uiState.update { it.copy(descriptionError = null) }
            is RecipeCreationEvent.ClearTimeError -> _uiState.update { it.copy(timeError = null) }
            is RecipeCreationEvent.ClearCaloriesError -> _uiState.update { it.copy(caloriesError = null) }
            is RecipeCreationEvent.ClearServesError -> _uiState.update { it.copy(servesError = null) }
            is RecipeCreationEvent.ClearIngredientsError -> _uiState.update { it.copy(ingredientsError = null) }
            is RecipeCreationEvent.ClearIngredientNameError -> {
                val newMap = _uiState.value.ingredientNameErrors.toMutableMap()
                newMap.remove(event.index)
                _uiState.update { it.copy(ingredientNameErrors = newMap) }
            }
            is RecipeCreationEvent.ClearIngredientQuantityError -> {
                val newMap = _uiState.value.ingredientQuantityErrors.toMutableMap()
                newMap.remove(event.index)
                _uiState.update { it.copy(ingredientQuantityErrors = newMap) }
            }
            is RecipeCreationEvent.ClearIngredientUnitError -> {
                val newMap = _uiState.value.ingredientUnitErrors.toMutableMap()
                newMap.remove(event.index)
                _uiState.update { it.copy(ingredientUnitErrors = newMap) }
            }
            is RecipeCreationEvent.ClearStepsError -> _uiState.update { it.copy(stepsError = null) }
            is RecipeCreationEvent.ClearStepImageError -> {
                val newMap = _uiState.value.stepImageErrors.toMutableMap()
                newMap.remove(event.index)
                _uiState.update { it.copy(stepImageErrors = newMap) }
            }
            is RecipeCreationEvent.ClearStepTitleError -> {
                val newMap = _uiState.value.stepTitleErrors.toMutableMap()
                newMap.remove(event.index)
                _uiState.update { it.copy(stepTitleErrors = newMap) }
            }
            is RecipeCreationEvent.ClearStepDescriptionError -> {
                val newMap = _uiState.value.stepDescriptionErrors.toMutableMap()
                newMap.remove(event.index)
                _uiState.update { it.copy(stepDescriptionErrors = newMap) }
            }
            is RecipeCreationEvent.ClearCopyError -> _uiState.update { it.copy(copyError = null) }
        }
    }

    private fun validateRecipe(recipe: Recipe, isCopying: Boolean): Boolean {
        var isValid = true
        var copyErr: String? = null
        var coverImgErr: String? = null
        var titleErr: String? = null
        var descErr: String? = null
        var timeErr: String? = null
        var calErr: String? = null
        var servesErr: String? = null
        var ingErr: String? = null
        val ingNameErrs = mutableMapOf<Int, String>()
        val ingQuantErrs = mutableMapOf<Int, String>()
        val ingUnitErrs = mutableMapOf<Int, String>()
        var stepsErr: String? = null
        val stepImgErrs = mutableMapOf<Int, String>()
        val stepTitleErrs = mutableMapOf<Int, String>()
        val stepDescErrs = mutableMapOf<Int, String>()

        if (recipe.recipePhotoId.isBlank()) { coverImgErr = "The cover image cannot be empty."; isValid = false }
        if (recipe.title.isBlank()) { titleErr = "The title cannot be empty."; isValid = false }
        if (recipe.description.isBlank()) { descErr = "The description cannot be empty."; isValid = false }
        if (recipe.cookTime <= 0) { timeErr = "The cook time must be greater than 0"; isValid = false }
        if (recipe.calories <= 0) { calErr = "The calories must be greater than 0"; isValid = false }
        if (recipe.serves <= 0) { servesErr = "The serves number must be greater than 0"; isValid = false }

        if (recipe.ingredients.isEmpty()) {
            ingErr = "The recipe must have at least one ingredient."
            isValid = false
        } else {
            recipe.ingredients.forEachIndexed { index, ingredient ->
                if (ingredient.ingredient.name.isBlank()) { ingNameErrs[index] = "The ingredient name cannot be empty."; isValid = false }
                if (ingredient.quantity <= 0.0) { ingQuantErrs[index] = "The quantity must be greater than 0."; isValid = false }
                if (ingredient.unit.isBlank()) { ingUnitErrs[index] = "The unit must be greater than 0"; isValid = false }
            }
        }

        if (recipe.steps.isEmpty()) {
            stepsErr = "The recipe must have at least one step."
            isValid = false
        } else {
            recipe.steps.forEachIndexed { index, step ->
                if (step.title.isBlank()) { stepTitleErrs[index] = "The step title cannot be empty"; isValid = false }
                if (step.description.isBlank()) { stepDescErrs[index] = "The step description cannot be empty"; isValid = false }
            }
        }

        if (isCopying) {
            val original = _uiState.value.originalRecipeForCopy
            if (original != null) {
                val hasChanged = recipe.title != original.title ||
                        recipe.description != original.description ||
                        recipe.cookTime != original.cookTime ||
                        recipe.calories != original.calories ||
                        recipe.serves != original.serves ||
                        recipe.ingredients != original.ingredients ||
                        recipe.steps != original.steps ||
                        recipe.cuisineType != original.cuisineType ||
                        recipe.dietaryRestrictions != original.dietaryRestrictions

                if (!hasChanged) {
                    copyErr = "You must modify the recipe before copying it"
                    isValid = false
                }
            }
        }

        _uiState.update { it.copy(
            coverImageError = coverImgErr,
            titleError = titleErr,
            descriptionError = descErr,
            timeError = timeErr,
            caloriesError = calErr,
            servesError = servesErr,
            ingredientsError = ingErr,
            ingredientNameErrors = ingNameErrs,
            ingredientQuantityErrors = ingQuantErrs,
            ingredientUnitErrors = ingUnitErrs,
            stepsError = stepsErr,
            stepImageErrors = stepImgErrs,
            stepTitleErrors = stepTitleErrs,
            stepDescriptionErrors = stepDescErrs,
            copyError = copyErr
        )}

        return isValid
    }

    private fun clearAllValidationErrors() {
        _uiState.update { it.copy(
            coverImageError = null,
            titleError = null,
            descriptionError = null,
            timeError = null,
            caloriesError = null,
            servesError = null,
            ingredientsError = null,
            ingredientNameErrors = emptyMap(),
            ingredientQuantityErrors = emptyMap(),
            ingredientUnitErrors = emptyMap(),
            stepsError = null,
            stepImageErrors = emptyMap(),
            stepTitleErrors = emptyMap(),
            stepDescriptionErrors = emptyMap(),
            copyError = null
        )}
    }
}
