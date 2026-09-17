package com.example.tastify.profile.hub.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastify.domain.GetCoursesUseCase
import com.example.tastify.domain.GetRecipesByIdsUseCase
import com.example.tastify.domain.GetRecipesUseCase
import com.example.tastify.domain.GetUserProfileUseCase
import com.example.tastify.domain.SetFollowingUseCase
import com.example.tastify.models.CustomCourse
import com.example.tastify.models.Recipe
import com.example.tastify.utils.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileHubViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getCoursesUseCase: GetCoursesUseCase,
    private val getRecipesByIdsUseCase: GetRecipesByIdsUseCase,
    private val getRecipesUseCase: GetRecipesUseCase,
    private val setFollowingUseCase: SetFollowingUseCase
) : ViewModel() {

    var state by mutableStateOf(ProfileHubState())
        private set

    private var hubDataJob: Job? = null
    private var loadedUserId: String? = null

    fun onEvent(event: ProfileHubEvent) {
        when (event) {
            is ProfileHubEvent.LoadHubData -> {
                loadHubData(event.userId)
            }
            is ProfileHubEvent.OnRecipeClicked -> {

            }
            is ProfileHubEvent.SetFollowing -> {
                setFollowing(event.currentUserId, event.targetUserId, event.follow)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadHubData(userId: String) {
        if (loadedUserId == userId && hubDataJob?.isActive == true) return

        loadedUserId = userId
        hubDataJob?.cancel()
        state = ProfileHubState(isLoading = true)

        hubDataJob = viewModelScope.launch {
            combine(
                getUserProfileUseCase(userId),
                getRecipesUseCase(ownerId = userId),
                observeCustomCoursesWithRecipes(userId)
            ) { user, recipes, coursesWithRecipes ->
                val (courses, courseRecipes) = coursesWithRecipes
                val isOwnProfile = userId == SessionManager.CURRENT_LOGGED_IN_USER_ID
                val visibleRecipes = if (isOwnProfile) recipes else recipes.filter { it.isPublic }
                ProfileHubState(
                    isLoading = false,
                    userProfile = user,
                    recentRecipes = visibleRecipes,
                    customCourses = courses,
                    customCourseRecipes = courseRecipes
                )
            }
                .catch { exception ->
                    state = state.copy(
                        isLoading = false,
                        error = exception.message ?: "Error loading profile data"
                    )
                }
                .collect { nextState ->
                    state = nextState
                }
        }
    }

    private fun setFollowing(currentUserId: String, targetUserId: String, follow: Boolean) {
        viewModelScope.launch {
            runCatching {
                setFollowingUseCase(
                    currentUserId = currentUserId,
                    targetUserId = targetUserId,
                    follow = follow
                )
            }.onFailure { exception ->
                state = state.copy(
                    error = exception.message ?: "Error updating follow status"
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeCustomCoursesWithRecipes(userId: String): Flow<Pair<List<CustomCourse>, List<Recipe>>> {
        return getCoursesUseCase(userId)
            .flatMapLatest { courses ->
                val recipeIds = courses.flatMap { it.recipeIds }.distinct()
                val recipesFlow = if (recipeIds.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    getRecipesByIdsUseCase(recipeIds)
                }
                val ownerRecipesFlow = if (userId == SessionManager.CURRENT_LOGGED_IN_USER_ID) {
                    getRecipesUseCase(ownerId = userId)
                } else {
                    flowOf(emptyList())
                }
                combine(recipesFlow, ownerRecipesFlow) { publicRecipes, ownerRecipes ->
                    courses to (publicRecipes + ownerRecipes).distinctBy { it.recipeId }
                }
            }
    }
}
