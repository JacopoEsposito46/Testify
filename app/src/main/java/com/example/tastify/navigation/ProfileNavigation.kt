package com.example.tastify.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.tastify.profile.ProfileScreen
import com.example.tastify.profile.analytics.ProfileAnalyticsScreen
import com.example.tastify.profile.analytics.logic.ProfileAnalyticsEvent
import com.example.tastify.profile.analytics.logic.ProfileAnalyticsViewModel
import com.example.tastify.profile.edit.EditProfileScreen
import com.example.tastify.profile.edit.logic.EditProfileViewModel
import com.example.tastify.profile.history.HistoryScreen
import com.example.tastify.profile.hub.ProfileHubScreen
import com.example.tastify.profile.hub.logic.ProfileHubEvent
import com.example.tastify.profile.hub.logic.ProfileHubViewModel
import com.example.tastify.profile.logic.ProfileViewModel
import com.example.tastify.profile.settings.SettingsScreen
import com.example.tastify.profile.settings.logic.SettingsViewModel
import com.example.tastify.notifications.logic.NotificationViewModel
import com.example.tastify.profile.history.logic.HistoryEvent
import com.example.tastify.profile.history.logic.HistoryViewModel
import com.example.tastify.recipe.social.ImportRecipeScreen
import com.example.tastify.recipe.weekly.WeeklyPlanningScreen
import com.example.tastify.recipe.weekly.logic.WeeklyPlanningViewModel
fun NavGraphBuilder.profileGraph(
    navActions: NavigationActions
) {
    composable<ProfileHubRoute> { backStackEntry ->
        val parentEntry = remember(backStackEntry) {
            navActions.navCtrl_r.getBackStackEntry(MainAppGraph)
        }
        val sharedProfileViewModel: ProfileViewModel = hiltViewModel(parentEntry)
        val profileState by sharedProfileViewModel.uiState.collectAsStateWithLifecycle()
        val userProfile = profileState.profile

        val hubViewModel: ProfileHubViewModel = hiltViewModel()
        val hubState = hubViewModel.state

        val notificationViewModel: NotificationViewModel = hiltViewModel(parentEntry)
        val notifState by notificationViewModel.state.collectAsStateWithLifecycle()

        LaunchedEffect(userProfile.internalID) {
            userProfile.internalID.let { userId ->
                hubViewModel.onEvent(ProfileHubEvent.LoadHubData(userId))
            }
        }

        ProfileHubScreen(
            userProfile = hubState.userProfile ?: userProfile,
            recentRecipes = hubState.recentRecipes,
            recentRecipesTitle = "My Recipes",
            isLoading = hubState.isLoading,
            onBack = { navActions.navigateBack() },
            onNavigateToSettings = { navActions.navigateToSettings() },
            onNavigateToHistory = { navActions.navigateToHistory() },
            onNavigateToPersonalInfo = { navActions.navigateToMyProfile() },
            onNavigateToAnalytics = { navActions.navigateToProfileAnalytics() },
            onNavigateToPlanning = { navActions.navigateToWeeklyPlanning() },
            onNavigateToRecipeProposalList = { navActions.navigateToRecipeProposalList() },
            onNavigateToCookBook = { navActions.navigateToCustomCourses() },
            onNavigateToRecipeList = { navActions.navigateToRecipeList("my_recipes") },
            onNavigateToNotifications = { navActions.navigateToNotifications() },
            onNavigateToChat = { navActions.navigateToChat() },
            unreadNotificationCount = notifState.unreadCount,
            onRecipeClick = { recipe -> navActions.navigateToRecipeDetail(recipe.recipeId) },
            onAddRecipe = { navActions.navigateToCreateRecipe() },
            onNavigateToImportRecipe = { navActions.navigateToImportRecipe() }
        )
    }

    composable<ImportRecipeRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<ImportRecipeRoute>()
        ImportRecipeScreen(
            sharedUrl = route.sharedUrl,
            onBack = { navActions.navigateBack() },
            onImportSuccess = { jsonString ->
                navActions.navigateToCreateRecipe(jsonString)
            }
        )
    }

    composable<ProfileAnalyticsRoute> { backStackEntry ->
        val parentEntry = remember(backStackEntry) {
            navActions.navCtrl_r.getBackStackEntry(MainAppGraph)
        }
        val profileViewModel: ProfileViewModel = hiltViewModel(parentEntry)
        val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()

        val analyticsViewModel: ProfileAnalyticsViewModel = hiltViewModel()
        val analyticsState = analyticsViewModel.state

        val currentUserId = profileState.profile.internalID

        LaunchedEffect(currentUserId) {
            if (currentUserId.isNotBlank() && currentUserId != "-1") {
                analyticsViewModel.onEvent(ProfileAnalyticsEvent.CalculateAnalytics(currentUserId))
            }
        }

        ProfileAnalyticsScreen(
            state = analyticsState,
            onBack = { navActions.navigateBack() }
        )
    }

    composable<MyProfileRoute> { backStackEntry ->
        val parentEntry = remember(backStackEntry) {
            navActions.navCtrl_r.getBackStackEntry(MainAppGraph)
        }
        val notificationViewModel: NotificationViewModel = hiltViewModel(parentEntry)
        val notifState by notificationViewModel.state.collectAsStateWithLifecycle()
        ProfileScreen(
            isOwner = true,
            onBack = { navActions.navigateBack() },
            onEdit = { navActions.navigateToEditProfile() },
            onRecipeClick = { recipe -> navActions.navigateToRecipeDetail(recipe.recipeId) }
        )
    }

    composable<OtherProfileRoute> { backStackEntry ->
        val parentEntry = remember(backStackEntry) {
            navActions.navCtrl_r.getBackStackEntry(MainAppGraph)
        }
        val notificationViewModel: NotificationViewModel = hiltViewModel(parentEntry)
        val notifState by notificationViewModel.state.collectAsStateWithLifecycle()
        val profileViewModel: ProfileViewModel = hiltViewModel(parentEntry)
        val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
        val route = backStackEntry.toRoute<OtherProfileRoute>()
        val hubViewModel: ProfileHubViewModel = hiltViewModel()
        val hubState = hubViewModel.state
        val currentUserProfile = profileState.profile
        val isFollowing = currentUserProfile.followedId.contains(route.userId)

        LaunchedEffect(route.userId) {
            hubViewModel.onEvent(ProfileHubEvent.LoadHubData(route.userId))
        }

        ProfileScreen(
            profile = hubState.userProfile,
            profileRecipes = hubState.recentRecipes,
            isLoading = hubState.isLoading,
            isOwner = false,
            onBack = { navActions.navigateBack() },
            onEdit = { navActions.navigateToMyProfile() },
            onRecipeClick = { recipe -> navActions.navigateToRecipeDetail(recipe.recipeId) },
            isFollowing = isFollowing,
            onFollowClick = {
                val currentUserId = currentUserProfile.internalID
                if (currentUserId.isNotBlank() && currentUserId != "-1") {
                    hubViewModel.onEvent(
                        ProfileHubEvent.SetFollowing(
                            currentUserId = currentUserId,
                            targetUserId = route.userId,
                            follow = !isFollowing
                        )
                    )
                }
            }
        )
    }

    composable<EditProfileRoute> { backStackEntry ->
        val parentEntry = remember(backStackEntry) {
            navActions.navCtrl_r.getBackStackEntry(MainAppGraph)
        }

        val editProfileViewModel: EditProfileViewModel = hiltViewModel(parentEntry)
        val notificationViewModel: NotificationViewModel = hiltViewModel(parentEntry)
        val notifState by notificationViewModel.state.collectAsStateWithLifecycle()

        EditProfileScreen(
            viewModel = editProfileViewModel,
            onBack = { navActions.navigateBack() },
            onNavigateToRecipeProposalList = { navActions.navigateToRecipeProposalList() },
            onNavigateToMyProfile = { navActions.navigateToProfileHub() },
            onNavigateToCookBook = { navActions.navigateToCustomCourses() },
            onNavigateToNotifications = { navActions.navigateToNotifications() },
            onNavigateToChat = { navActions.navigateToChat() },
            unreadNotificationCount = notifState.unreadCount
        )
    }

    composable<HistoryRoute> { backStackEntry ->
        val historyViewModel: HistoryViewModel = hiltViewModel()

        HistoryScreen(
            onNavigateBack = { navActions.navigateBack() },
            onNavigateToRecipe = { recipeId -> navActions.navigateToRecipeDetail(recipeId) },
            viewModel = historyViewModel,
            onDeleteClick = { recipeId ->
                historyViewModel.onEvent( HistoryEvent.RemoveRecipe(recipeId))
            }
        )
    }

    composable<SettingsRoute> {
        val settingsViewModel: SettingsViewModel = hiltViewModel()
        val settingsState = settingsViewModel.state

        LaunchedEffect(settingsState.logoutSuccess) {
            if (settingsState.logoutSuccess) {
                navActions.navigateToLoginAndClearBackStack()
            }
        }

        SettingsScreen(
            state = settingsState,
            onEvent = settingsViewModel::onEvent,
            onBack = { navActions.navigateBack() },
            onNavigateToEditProfile = { navActions.navigateToEditProfile() }
        )
    }

    composable<WeeklyPlanningRoute> {
        val viewModel: WeeklyPlanningViewModel = hiltViewModel()
        WeeklyPlanningScreen(
            viewModel = viewModel,
            onNavigateBack = { navActions.navigateBack() },
            onNavigateToRecipe = { recipeId -> navActions.navigateToRecipeDetail(recipeId) }
        )
    }
}
