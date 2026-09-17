package com.example.tastify.navigation

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.tastify.chatbot.ChatScreen
import com.example.tastify.chatbot.logic.ChatViewModel
import com.example.tastify.models.NotificationType
import com.example.tastify.notifications.components.NotificationsScreen
import com.example.tastify.notifications.logic.NotificationViewModel
import com.example.tastify.recipe.explore.RecipeExploreScreen
import com.example.tastify.recipe.explore.logic.RecipeExploreEvent
import com.example.tastify.recipe.explore.logic.RecipeExploreViewModel

fun NavGraphBuilder.mainGraph(
    navCtrl_r: NavHostController
) {
    val navActions = NavigationActions(navCtrl_r)

    navigation<MainAppGraph>(startDestination = MainRoute) {
        composable<MainRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navCtrl_r.getBackStackEntry<MainAppGraph>()
            }
            val viewModel: RecipeExploreViewModel = hiltViewModel(parentEntry)
            val notificationViewModel: NotificationViewModel = hiltViewModel(parentEntry)
            val notifState by notificationViewModel.state.collectAsStateWithLifecycle()

            RecipeExploreScreen(
                viewModel = viewModel,
                onRecipeClick = { selectedRecipeItem ->
                    navActions.navigateToRecipeDetail(selectedRecipeItem.recipeId)
                },
                onToggleFavourite = { recipeId ->
                    viewModel.onEvent(RecipeExploreEvent.ToggleFavourite(recipeId))
                },
                onNavigateToRecipeProposalList = { navActions.navigateToRecipeProposalList() },
                onNavigateToMyProfile = { navActions.navigateToProfileHub() },
                onNavigateToFilters = { navActions.navigateToExploreFilters() },
                onNavigateToCategory = { category -> navActions.navigateToExploreCategory(category) },
                onNavigateToCookBook = { navActions.navigateToCustomCourses() },
                onNavigateToNotifications = { navActions.navigateToNotifications() },
                onNavigateToChat = { navActions.navigateToChat() },
                onAddRecipe = { navActions.navigateToCreateRecipe() },
                onNavigateToImportRecipe = { navActions.navigateToImportRecipe() },
                unreadNotificationCount = notifState.unreadCount
            )
        }

        composable<NotificationsRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navCtrl_r.getBackStackEntry<MainAppGraph>()
            }
            val viewModel: NotificationViewModel = hiltViewModel(parentEntry)
            val state by viewModel.state.collectAsStateWithLifecycle()

            // Suppress snackbar while on this screen
            DisposableEffect(Unit) {
                viewModel.suppressSnackbar(true)
                viewModel.setNotificationsScreenVisible(true)
                onDispose {
                    viewModel.suppressSnackbar(false)
                    viewModel.setNotificationsScreenVisible(false)
                }
            }

            NotificationsScreen(
                notifications = state.notifications,
                searchQuery = state.searchQuery,
                isSelectionMode = state.isSelectionMode,
                selectedIds = state.selectedIds,
                onEvent = viewModel::onEvent,
                onNotificationClick = { notification ->
                    notification.relatedRecipeId?.let { recipeId ->
                        when (notification.type) {
                            NotificationType.REVIEW_RECEIVED -> navActions.navigateToReviews(recipeId)
                            NotificationType.DUPLICATION -> navActions.navigateToRecipeDetail(recipeId)
                            NotificationType.RECOMMENDATION -> navActions.navigateToRecipeDetail(recipeId)
                            NotificationType.COMMENT_RECEIVED -> navActions.navigateToComments(recipeId)
                        }
                    }
                },
                onNavigateBack = { navActions.navigateBack() },
                onNavigateToRecipeProposalList = { navActions.navigateToRecipeProposalList() },
                onNavigateToMyProfile = { navActions.navigateToProfileHub() },
                onNavigateToCookBook = { navActions.navigateToCustomCourses() },
                onNavigateToChat = { navActions.navigateToChat() },
                unreadNotificationCount = state.unreadCount
            )
        }
        composable<ChatRoute> { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navCtrl_r.getBackStackEntry<MainAppGraph>()
            }
            val viewModel: ChatViewModel = hiltViewModel()
            val notificationViewModel: NotificationViewModel = hiltViewModel(parentEntry)
            val notifState by notificationViewModel.state.collectAsStateWithLifecycle()

            ChatScreen(
                viewModel = viewModel,
                unreadNotificationCount = notifState.unreadCount,
                onNavigateBack = { navActions.navigateBack() },
                onNavigateToRecipe = { recipeId -> navActions.navigateToRecipeDetail(recipeId) },
                onNavigateToHome = { navActions.navigateToRecipeProposalList() },
                onNavigateToCookBook = { navActions.navigateToCustomCourses() },
                onNavigateToNotifications = { navActions.navigateToNotifications() },
                onNavigateToProfile = { navActions.navigateToProfileHub() },
                onNavigateToChat = { }
            )
        }

        profileGraph(navActions)
        recipeGraph(navActions)
    }
}
