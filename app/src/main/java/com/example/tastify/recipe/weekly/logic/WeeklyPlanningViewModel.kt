package com.example.tastify.recipe.weekly.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastify.domain.GetCoursesUseCase
import com.example.tastify.domain.GetFavoriteRecipesReferencesUseCase
import com.example.tastify.domain.GetImportedRecipesUseCase
import com.example.tastify.domain.GetRecipesByIdsUseCase
import com.example.tastify.domain.GetRecipesUseCase
import com.example.tastify.domain.WeeklyPlanUseCases
import com.example.tastify.models.PlannedMeal
import com.example.tastify.models.UserWeeklyPlan
import com.example.tastify.notifications.SnackbarManager
import com.example.tastify.plan.models.DayDate
import com.example.tastify.plan.models.MealSlot
import com.example.tastify.plan.models.PlannedRecipe
import com.example.tastify.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class WeeklyPlanningViewModel @Inject constructor(
    private val weeklyPlanUseCases: WeeklyPlanUseCases,
    private val getCoursesUseCase: GetCoursesUseCase,
    private val getFavoriteRecipesReferencesUseCase: GetFavoriteRecipesReferencesUseCase,
    private val getRecipesByIdsUseCase: GetRecipesByIdsUseCase,
    private val getImportedRecipesUseCase: GetImportedRecipesUseCase,
    private val getRecipesUseCase: GetRecipesUseCase,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklyPlanningUiState())
    val uiState: StateFlow<WeeklyPlanningUiState> = _uiState.asStateFlow()

    private val currentUserId = SessionManager.CURRENT_LOGGED_IN_USER_ID ?: ""
    private val today = LocalDate.now()
    private val startOfWeek = today.with(DayOfWeek.MONDAY)
    private val dayFormatter = DateTimeFormatter.ofPattern("EEE")
    
    private val currentSelectedDateStr = MutableStateFlow(today.toString())
    
    private val bottomSheetSearchQuery = MutableStateFlow("")
    private val bottomSheetSelectedFilter = MutableStateFlow("All")

    init {
        initDays()
        observeData()
    }

    private fun initDays() {
        val days = (0..6).map { offset ->
            val date = startOfWeek.plusDays(offset.toLong())
            DayDate(
                dayName = date.format(dayFormatter).uppercase(),
                dayNumber = date.dayOfMonth.toString(),
                isSelected = date == today
            )
        }
        _uiState.update { it.copy(days = days) }
    }
    
    private fun getRealDateForDayNumber(dayNumber: String): String {
        val offset = (0..6).find { startOfWeek.plusDays(it.toLong()).dayOfMonth.toString() == dayNumber } ?: 0
        return startOfWeek.plusDays(offset.toLong()).toString()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        if (currentUserId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                weeklyPlanUseCases.getUserPlan(currentUserId),
                getCoursesUseCase(currentUserId),
                getFavoriteRecipesReferencesUseCase(currentUserId),
                getImportedRecipesUseCase(currentUserId),
                getRecipesUseCase(currentUserId)
            ) { userPlan, coursesList, favoritesList, importedRecipes, myRecipes ->
                val plan = userPlan ?: UserWeeklyPlan(userId = currentUserId)
                Tuple5(plan, coursesList, favoritesList, importedRecipes, myRecipes)
            }.combine(currentSelectedDateStr) { tuple5, selectedDate ->
                Tuple6(tuple5.t1, tuple5.t2, tuple5.t3, tuple5.t4, tuple5.t5, selectedDate)
            }.flatMapLatest { (plan, coursesList, favoritesList, importedRecipes, myRecipes, selectedDate) ->
                val favoriteIds = favoritesList.map { it.recipeId }
                val courseRecipeIds = coursesList.flatMap { it.recipeIds }
                val plannedRecipeIds = plan.plannedMeals.map { it.recipeId }
                val importedRecipeIds = importedRecipes.map { it.recipeId }
                val myRecipeIds = myRecipes.map { it.recipeId }
                
                val allRecipeIds = (favoriteIds + courseRecipeIds + plannedRecipeIds + importedRecipeIds + myRecipeIds).distinct()
                
                val recipesFlow = if (allRecipeIds.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    getRecipesByIdsUseCase(allRecipeIds)
                }

                combine(
                    recipesFlow,
                    bottomSheetSearchQuery,
                    bottomSheetSelectedFilter
                ) { recipesList, query, filter ->
                    val accessibleRecipes = (recipesList + importedRecipes + myRecipes).distinctBy { it.recipeId }
                    val recipesById = accessibleRecipes.associateBy { it.recipeId }
                    val favouriteRecipes = favoriteIds.mapNotNull { recipesById[it] }

                    // Build meal slots for selectedDate
                    val mealsForDay = plan.plannedMeals.filter { it.date == selectedDate }
                    val mealSlots = listOf("Breakfast", "Lunch", "Snack", "Dinner").map { mealType ->
                        val recipesForSlot = mealsForDay.filter { it.mealType == mealType }.mapNotNull { plannedMeal ->
                            val recipe = recipesById[plannedMeal.recipeId]
                            if (recipe != null) {
                                PlannedRecipe(
                                    id = recipe.recipeId,
                                    title = recipe.title,
                                    imageUrl = recipe.recipePhotoId,
                                    rating = recipe.rating,
                                    timeMinutes = recipe.cookTime,
                                    calories = recipe.calories,
                                    ingredientsSummary = recipe.ingredients.take(3).joinToString(", ") { it.ingredient.name } + if(recipe.ingredients.size > 3) "..." else ""
                                )
                            } else null
                        }
                        
                        MealSlot(
                            type = mealType,
                            time = getTimeForMealType(mealType),
                            totalCalories = recipesForSlot.sumOf { it.calories },
                            recipes = recipesForSlot
                        )
                    }

                    // Compute displayed recipes
                    val baseRecipes = if (query.isNotEmpty()) {
                        (favouriteRecipes + importedRecipes + myRecipes + coursesList.flatMap { course -> course.recipeIds.mapNotNull { recipesById[it] } }).distinctBy { it.recipeId }
                    } else {
                        when (filter) {
                            "All" -> (favouriteRecipes + importedRecipes + myRecipes + coursesList.flatMap { course -> course.recipeIds.mapNotNull { recipesById[it] } }).distinctBy { it.recipeId }
                            "Favorites" -> favouriteRecipes
                            "My Recipes" -> myRecipes
                            "Imported" -> importedRecipes
                            else -> {
                                val course = coursesList.find { it.title == filter }
                                course?.recipeIds?.mapNotNull { recipesById[it] } ?: emptyList()
                            }
                        }
                    }

                    val displayedRecipes = if (query.isNotEmpty()) {
                        baseRecipes.filter { it.title.contains(query, ignoreCase = true) }
                    } else {
                        baseRecipes
                    }

                    _uiState.value.copy(
                        isLoading = false,
                        mealSlots = mealSlots,
                        customCourses = coursesList,
                        favoriteRecipes = favouriteRecipes,
                        importedRecipes = importedRecipes,
                        myRecipes = myRecipes,
                        recipesById = recipesById,
                        displayedRecipes = displayedRecipes,
                        selectedFilter = filter
                    )
                }
            }.collect { nextState ->
                _uiState.value = nextState
            }
        }
    }

    private fun getTimeForMealType(type: String): String = when (type) {
        "Breakfast" -> "08:30"
        "Lunch" -> "13:00"
        "Snack" -> "16:30"
        "Dinner" -> "20:00"
        else -> "00:00"
    }

    fun selectDay(dayNumber: String) {
        val realDate = getRealDateForDayNumber(dayNumber)
        currentSelectedDateStr.value = realDate
        
        _uiState.update { currentState ->
            val updatedDays = currentState.days.map { day ->
                day.copy(isSelected = day.dayNumber == dayNumber)
            }
            currentState.copy(days = updatedDays)
        }
    }

    fun openAddRecipeBottomSheet(mealType: String) {
        _uiState.update { 
            it.copy(
                isBottomSheetOpen = true,
                selectedMealTypeForAdd = mealType,
                selectedDateForAdd = currentSelectedDateStr.value
            )
        }
    }

    fun closeBottomSheet() {
        _uiState.update { 
            it.copy(
                isBottomSheetOpen = false,
                selectedMealTypeForAdd = null,
                selectedDateForAdd = null
            )
        }
    }

    fun addRecipeToSlot(recipeId: String) {
        val date = _uiState.value.selectedDateForAdd ?: return
        val mealType = _uiState.value.selectedMealTypeForAdd ?: return
        
        viewModelScope.launch {
            val meal = PlannedMeal(date = date, mealType = mealType, recipeId = recipeId)
            weeklyPlanUseCases.addMealToPlan(currentUserId, meal)
            snackbarManager.showMessage("Recipe added to $mealType")
        }
    }

    fun removeRecipeFromSlot(mealType: String, recipeId: String) {
        val date = currentSelectedDateStr.value
        viewModelScope.launch {
            weeklyPlanUseCases.removeMealFromPlan(currentUserId, date, mealType, recipeId)
            snackbarManager.showMessage("Recipe removed from $mealType")
        }
    }

    fun setSearchQuery(query: String) {
        bottomSheetSearchQuery.value = query
    }

    fun setSelectedFilter(filter: String) {
        bottomSheetSelectedFilter.value = filter
    }
}
