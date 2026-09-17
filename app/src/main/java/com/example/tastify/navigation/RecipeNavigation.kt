package com.example.tastify.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.example.tastify.recipe.courses.CustomCourses
import com.example.tastify.recipe.courses.logic.CustomCoursesEvent
import com.example.tastify.recipe.courses.logic.CustomCoursesViewModel
import com.example.tastify.comment.CommentsScreen
import com.example.tastify.comment.logic.CommentEvent
import com.example.tastify.comment.logic.CommentViewModel
import com.example.tastify.recipe.creation.RecipeCreationScreen
import com.example.tastify.recipe.list.RecipeList
import com.example.tastify.recipe.details.RecipeDetailScreen
import com.example.tastify.recipe.explore.FilterScreen
import com.example.tastify.recipe.explore.RecipeExploreScreen
import com.example.tastify.recipe.explore.CategoryGridScreen
import com.example.tastify.recipe.creation.logic.RecipeCreationViewModel
import com.example.tastify.recipe.details.logic.RecipeDetailViewModel
import com.example.tastify.recipe.explore.logic.RecipeExploreEvent
import com.example.tastify.recipe.explore.logic.RecipeExploreViewModel
import com.example.tastify.recipe.explore.logic.CategoryGridViewModel
import com.example.tastify.review.ReviewScreen
import com.example.tastify.review.WriteReviewScreen
import com.example.tastify.review.logic.ReviewEvent
import com.example.tastify.review.logic.ReviewViewModel
import com.example.tastify.recipe.stats.RecipeStatistics
import com.example.tastify.recipe.stats.logic.RecipeStatsViewModel
import com.example.tastify.notifications.logic.NotificationViewModel
import com.example.tastify.profile.logic.ProfileViewModel
import com.example.tastify.report.ReportScreen
import com.example.tastify.report.logic.ReportEvent
import com.example.tastify.report.logic.ReportViewModel
import com.example.tastify.utils.SessionManager

fun NavGraphBuilder.recipeGraph(
    navActions: NavigationActions
) {
    composable<RecipeProposalRoute> {
        val viewModel: RecipeDetailViewModel = hiltViewModel()
        RecipeDetailScreen(
            viewModel = viewModel,
            onNavigateBack = { navActions.navigateBack() },
            onNavigateToStats = { navActions.navigateToStatistics(it.arguments?.getString("recipeId") ?: "") },
            onNavigateToReview = { navActions.navigateToReviews(it.arguments?.getString("recipeId") ?: "")},
            onNavigateToComment = { navActions.navigateToComments(it.arguments?.getString("recipeId") ?: "")},
            onNavigateToReport = { navActions.navigateToReport(it.arguments?.getString("recipeId") ?: "")},
            onNavigateToProfile = { navActions.navigateToProfileHub()},
            onNavigateToOtherProfile = { id -> navActions.navigateToOtherProfile(id)},
            onNavigateToEditRecipeProposal = { navActions.navigateToEditRecipe(it.arguments?.getString("recipeId") ?: "") },
            onNavigateToCopyRecipeProposal = { navActions.navigateToCopyRecipe(it.arguments?.getString("recipeId") ?: "") }
        )
    }
    composable<CommentsRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<CommentsRoute>()
        val viewModel: CommentViewModel = hiltViewModel()
        val mainEntry = remember(backStackEntry) {
            navActions.navCtrl_r.getBackStackEntry(MainAppGraph)
        }
        val profileViewModel: ProfileViewModel = hiltViewModel(mainEntry)
        val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(route.recipeId) {
            viewModel.onEvent(CommentEvent.LoadComments(route.recipeId))
        }

        CommentsScreen(
            viewModel = viewModel,
            onBack = { navActions.navigateBack() },
            currentUserNickname = profileState.profile.username.ifBlank { profileState.profile.name },
            currentUserProfileImageUri = profileState.profile.profilePictureUrl ?: "",
            currentUserFrame = profileState.profile.chosenFrame,
            currentUserRole = profileState.profile.cookingRole.displayName
        )
    }

    composable<ReviewsRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<ReviewsRoute>()
        val viewModel: ReviewViewModel = hiltViewModel()

        LaunchedEffect(route.recipeId) {
            viewModel.onEvent(ReviewEvent.LoadReviews(route.recipeId))
        }

        ReviewScreen(
            viewModel = viewModel,
            onBack = { navActions.navigateBack() },
            onWriteReview = { navActions.navigateToWriteReview(route.recipeId) },
            onNavigateToProfile = { userId -> navActions.navigateToOtherProfile(userId) }
        )
    }

    composable<WriteReviewRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<WriteReviewRoute>()
        val viewModel: ReviewViewModel = hiltViewModel()
        val mainEntry = remember(backStackEntry) {
            navActions.navCtrl_r.getBackStackEntry(MainAppGraph)
        }
        val profileViewModel: ProfileViewModel = hiltViewModel(mainEntry)
        val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(route.recipeId) {
            viewModel.loadRecipe(route.recipeId)
            viewModel.onEvent(ReviewEvent.LoadReviews(route.recipeId))
        }

        val recipe by viewModel.recipe.collectAsStateWithLifecycle()
        val reviewState by viewModel.state.collectAsStateWithLifecycle()
        val safeUserId = SessionManager.CURRENT_LOGGED_IN_USER_ID ?: ""

        // Navigate back only after notification sent
        LaunchedEffect(reviewState.submitSuccess) {
            if (reviewState.submitSuccess) {
                navActions.navigateBack()
            }
        }

        WriteReviewScreen(
            recipe = recipe,
            onBack = { navActions.navigateBack() },
            onSubmit = { rating, text, uris ->
                viewModel.onEvent(ReviewEvent.UpdateDraftReview(rating, text))
                uris.forEach { uri ->
                    viewModel.onEvent(ReviewEvent.AddDraftReviewImage(uri))
                }
                viewModel.onEvent(
                    ReviewEvent.SubmitReview(
                        authorId = safeUserId,
                        authorNickname = profileState.profile.username.ifBlank { profileState.profile.name },
                        authorProfileImageUri = profileState.profile.profilePictureUrl ?: "",
                        authorFrame = profileState.profile.chosenFrame,
                        authorRole = profileState.profile.cookingRole.displayName
                    )
                )
            }
        )
    }

    composable<ReportRoute> { backStackEntry ->
        val route: ReportRoute = backStackEntry.toRoute()
        val viewModel: ReportViewModel = hiltViewModel()
        val safeUserId = SessionManager.CURRENT_LOGGED_IN_USER_ID ?: ""

        LaunchedEffect(route.recipeId) {
            viewModel.onEvent(ReportEvent.LoadReport(route.recipeId, safeUserId))
        }

        ReportScreen(
            viewModel = viewModel,
            onBack = { navActions.navigateBack() }
        )
    }

    navigation<ExploreGraph>(startDestination = RecipeProposalListRoute) {
        composable<RecipeProposalListRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navActions.navCtrl_r.getBackStackEntry<MainAppGraph>()
            }
            val mainEntry = remember(backStackEntry) {
                navActions.navCtrl_r.getBackStackEntry(MainAppGraph)
            }
            val viewModel: RecipeExploreViewModel = hiltViewModel(parentEntry)
            val notificationViewModel: NotificationViewModel = hiltViewModel(mainEntry)
            val notifState by notificationViewModel.state.collectAsStateWithLifecycle()
            RecipeExploreScreen(
                viewModel = viewModel,
                onRecipeClick = { selectedRecipeItem ->
                    navActions.navigateToRecipeDetail(selectedRecipeItem.recipeId)
                },
                onToggleFavourite = { id -> viewModel.onEvent(RecipeExploreEvent.ToggleFavourite(id)) },
                onNavigateToRecipeProposalList = { navActions.navigateToRecipeProposalList() },
                onNavigateToMyProfile = { navActions.navigateToProfileHub() },
                onNavigateToFilters = { navActions.navigateToExploreFilters() },
                onNavigateToCategory = { category -> navActions.navigateToExploreCategory(category) },
                onNavigateToCookBook = { navActions.navigateToCustomCourses() },
                onNavigateToNotifications = { navActions.navigateToNotifications() },
                onAddRecipe = { navActions.navigateToCreateRecipe() },
                onNavigateToImportRecipe = { navActions.navigateToImportRecipe() },
                onNavigateToChat = { navActions.navigateToChat() },
                unreadNotificationCount = notifState.unreadCount
            )
        }

        composable<ExploreFiltersRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navActions.navCtrl_r.getBackStackEntry<MainAppGraph>()
            }
            val viewModel: RecipeExploreViewModel = hiltViewModel(parentEntry)
            FilterScreen(
                viewModel = viewModel,
                onBack = { navActions.navigateBack() },
                onApply = { navActions.navigateBack() }
            )
        }

        composable<ExploreCategoryRoute> {
            val viewModel: CategoryGridViewModel = hiltViewModel()
            CategoryGridScreen(
                viewModel = viewModel,
                onRecipeClick = { recipe -> navActions.navigateToRecipeDetail(recipe.recipeId) },
                onNavigateBack = { navActions.navigateBack() }
            )
        }
    }

    navigation<MyRecipesGraph>(startDestination = CustomCoursesRoute) {
        composable<CustomCoursesRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navActions.navCtrl_r.getBackStackEntry<MyRecipesGraph>()
            }
            val mainEntry = remember(backStackEntry) {
                navActions.navCtrl_r.getBackStackEntry(MainAppGraph)
            }
            val viewModel: CustomCoursesViewModel = hiltViewModel(parentEntry)
            val notificationViewModel: NotificationViewModel = hiltViewModel(mainEntry)
            val notifState by notificationViewModel.state.collectAsStateWithLifecycle()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            CustomCourses(
                state = state,
                onBack = { navActions.navigateBack() },
                onNavigateToRecipeProposalList = { navActions.navigateToRecipeProposalList() },
                onNavigateToMyProfile = { navActions.navigateToProfileHub() },
                onNavigateToCookBook = { navActions.navigateToCustomCourses() },
                onNavigateToNotifications = { navActions.navigateToNotifications() },
                onNavigateToChat = { navActions.navigateToChat() },
                unreadNotificationCount = notifState.unreadCount,
                onNavigateToRecipeList = { courseId -> navActions.navigateToRecipeList(courseId) },
                onCreateCourse = { name ->
                    viewModel.onEvent(CustomCoursesEvent.CreateCourse(name))
                },
                onDeleteClick = { courseId ->
                    viewModel.onEvent(CustomCoursesEvent.DeleteCourse(courseId))
                }
            )
        }

        composable<MyRecipesFiltersRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navActions.navCtrl_r.getBackStackEntry<MyRecipesGraph>()
            }
            val viewModel: RecipeExploreViewModel = hiltViewModel(parentEntry)
            FilterScreen(
                viewModel = viewModel,
                onBack = { navActions.navigateBack() },
                onApply = { navActions.navigateBack() }
            )
        }
    }

    composable<RecipeStatsRoute> {
        val viewModel: RecipeStatsViewModel = hiltViewModel()
        RecipeStatistics(
            viewModel = viewModel,
            onBack = { navActions.navigateBack() }
        )
    }

    composable<CreateNewRecipeProposalRoute> {
        val viewModel: RecipeCreationViewModel = hiltViewModel()
        RecipeCreationScreen(
            viewModel = viewModel,
            isEditing = false,
            isCopying = false,
            onNavigateBack = { navActions.navigateBack() },
            onPublishSuccess = { navActions.navigateToMainAndClear() }
        )
    }

    composable<EditRecipeProposalRoute> {
        val viewModel: RecipeCreationViewModel = hiltViewModel()
        RecipeCreationScreen(
            viewModel = viewModel,
            isEditing = true,
            isCopying = false,
            onNavigateBack = { navActions.navigateBack() },
            onPublishSuccess = { navActions.navigateBack() }
        )
    }

    composable<CopyRecipeProposalRoute> {
        val viewModel: RecipeCreationViewModel = hiltViewModel()
        RecipeCreationScreen(
            viewModel = viewModel,
            isEditing = false,
            isCopying = true,
            onNavigateBack = { navActions.navigateBack() },
            onPublishSuccess = { navActions.navigateToMainAndClear() }
        )
    }

    composable<RecipeListRoute> { backStackEntry ->
        val route: RecipeListRoute = backStackEntry.toRoute()
        val parentEntry = remember(backStackEntry) {
            navActions.navCtrl_r.getBackStackEntry<MainAppGraph>()
        }
        val viewModel: CustomCoursesViewModel = hiltViewModel(parentEntry)
        val notificationViewModel: NotificationViewModel = hiltViewModel(parentEntry)
        val notifState by notificationViewModel.state.collectAsStateWithLifecycle()
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        val recipes = if (route.courseId.isNullOrBlank() || route.courseId == "null") {
            state.favouriteRecipes
        } else if (route.courseId == "imported") {
            state.importedRecipes
        } else if (route.courseId == "my_recipes") {
            state.myRecipes
        } else {
            val course = state.courses.find { it.id == route.courseId }
            val recipesById = state.savedRecipes.associateBy { it.recipeId }
            course?.recipeIds.orEmpty().mapNotNull { recipesById[it] }
        }

        RecipeList(
            recipes = recipes,
            favoriteRecipeIds = state.favoriteRecipeIds,
            onBack = { navActions.navigateBack() },
            onRecipeClick = { recipe -> navActions.navigateToRecipeDetail(recipe.recipeId) },
            onFilterClick = null,
            onRemoveRecipe = if (route.courseId.isNullOrBlank() || route.courseId == "null" || route.courseId == "imported" || route.courseId == "my_recipes") {
                null
            } else {
                { recipe ->
                    viewModel.onEvent(
                        CustomCoursesEvent.RemoveRecipeFromCourse(
                            courseId = route.courseId,
                            recipeId = recipe.recipeId
                        )
                    )
                }
            },
            onToggleFavourite = { recipeId ->
                viewModel.onEvent(CustomCoursesEvent.ToggleFavourite(recipeId))
            }
        )
    }
}