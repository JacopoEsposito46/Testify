package com.example.tastify.navigation

import androidx.navigation.NavHostController
import com.example.tastify.models.ExploreCategory
import kotlinx.serialization.Serializable
import com.example.tastify.utils.SessionManager

@Serializable
object HistoryRoute

@Serializable
object LoginGraph

@Serializable
object MainAppGraph

@Serializable
object ExploreGraph

@Serializable
object MyRecipesGraph

@Serializable
object LoginRoute

@Serializable
object MainRoute

@Serializable
object MyProfileRoute

@Serializable
object ProfileHubRoute

@Serializable
object ProfileAnalyticsRoute

@Serializable
data class OtherProfileRoute(val userId: String)

@Serializable
data class RecipeProposalRoute(val recipeId: String)

@Serializable
data class RecipeStatsRoute(val recipeId: String)

@Serializable
object EditProfileRoute

@Serializable
data class ReportRoute(val recipeId: String)

@Serializable
object RecipeProposalListRoute

@Serializable
object ExploreFiltersRoute

@Serializable
object MyRecipesFiltersRoute

@Serializable
object CustomCoursesRoute

@Serializable
data class CreateNewRecipeProposalRoute(val importedDataJson: String? = null)

@Serializable
data class EditRecipeProposalRoute(val editRecipeId: String)

@Serializable
data class CopyRecipeProposalRoute(val copyRecipeId: String)

@Serializable
data class ExploreCategoryRoute(val category: ExploreCategory)

@Serializable
data class CommentsRoute(val recipeId: String)

@Serializable
data class RecipeListRoute(val courseId: String? = null)

@Serializable
data class ReviewsRoute(val recipeId: String)

@Serializable
data class WriteReviewRoute(val recipeId: String)

@Serializable
object NotificationsRoute

@Serializable
data class ImportRecipeRoute(
    val sharedUrl: String? = null
)

@Serializable
object SettingsRoute

@Serializable
object ChatRoute

@Serializable
object WeeklyPlanningRoute

class NavigationActions(val navCtrl_r: NavHostController) {

    fun navigateToHistory() {
        navCtrl_r.navigate(HistoryRoute)
    }

    fun navigateToMainAppGraph() {
        navCtrl_r.navigate(MainAppGraph) {
            popUpTo(LoginGraph) { inclusive = true }
        }
    }

    fun navigateToSignIn() {
        navCtrl_r.navigate(LoginRoute)
    }

    fun navigateToExploreCategory(category: ExploreCategory) {
        navCtrl_r.navigate(ExploreCategoryRoute(category))
    }

    fun navigateToMyProfile() {
        navCtrl_r.navigate(MyProfileRoute)
    }

    fun navigateToProfileHub() {
        navCtrl_r.navigate(ProfileHubRoute)
    }

    fun navigateToProfileAnalytics() {
        navCtrl_r.navigate(ProfileAnalyticsRoute)
    }

    fun navigateToOtherProfile(userId: String) {
        if (userId == SessionManager.CURRENT_LOGGED_IN_USER_ID) {
            navCtrl_r.navigate(ProfileHubRoute)
        } else {
            navCtrl_r.navigate(OtherProfileRoute(userId))
        }
    }

    fun navigateToStatistics(recipeId: String) {
        navCtrl_r.navigate(RecipeStatsRoute(recipeId))
    }

    fun navigateToEditProfile() {
        navCtrl_r.navigate(EditProfileRoute)
    }

    fun navigateToReport(recipeId: String) {
        navCtrl_r.navigate(ReportRoute(recipeId))
    }

    fun navigateToRecipeProposalList() {
        navCtrl_r.navigate(ExploreGraph)
    }

    fun navigateToCustomCourses() {
        navCtrl_r.navigate(MyRecipesGraph)
    }

    fun navigateToExploreFilters() {
        navCtrl_r.navigate(ExploreFiltersRoute)
    }

    fun navigateToMyRecipesFilters() {
        navCtrl_r.navigate(MyRecipesFiltersRoute)
    }

    fun navigateToRecipeDetail(recipeId: String) {
        navCtrl_r.navigate(RecipeProposalRoute(recipeId))
    }

    fun navigateToCreateRecipe(importedDataJson: String? = null) {
        navCtrl_r.navigate(CreateNewRecipeProposalRoute(importedDataJson))
    }

    fun navigateToEditRecipe(recipeId: String) {
        navCtrl_r.navigate(EditRecipeProposalRoute(recipeId))
    }

    fun navigateToCopyRecipe(recipeId: String) {
        navCtrl_r.navigate(CopyRecipeProposalRoute(recipeId))
    }

    fun navigateToComments(recipeId: String) {
        navCtrl_r.navigate(CommentsRoute(recipeId))
    }

    fun navigateToReviews(recipeId: String) {
        navCtrl_r.navigate(ReviewsRoute(recipeId))
    }

    fun navigateToRecipeList(courseId: String? = null) {
        navCtrl_r.navigate(RecipeListRoute(courseId = courseId))
    }

    fun navigateToFavorites() {
        navCtrl_r.navigate(RecipeListRoute())
    }

    fun navigateToWriteReview(recipeId: String) {
        navCtrl_r.navigate(WriteReviewRoute(recipeId))
    }

    fun navigateToNotifications() {
        navCtrl_r.navigate(NotificationsRoute)
    }

    fun navigateToImportRecipe(sharedUrl: String? = null) {
        navCtrl_r.navigate(ImportRecipeRoute(sharedUrl))
    }

    fun navigateBack() {
        navCtrl_r.popBackStack()
    }

    fun navigateToMainAndClear() {
        navCtrl_r.navigate(MainRoute) {
            popUpTo(MainRoute) { inclusive = true }
        }
    }
    fun navigateToSettings() {
        navCtrl_r.navigate(SettingsRoute)
    }
    fun navigateToChat() {
        navCtrl_r.navigate(ChatRoute) {
            launchSingleTop = true
        }
    }
    fun navigateToLoginAndClearBackStack() {
        navCtrl_r.navigate(LoginGraph) {
            popUpTo(MainAppGraph) {
                inclusive = true
            }
        }
    }

    fun navigateToWeeklyPlanning() {
        navCtrl_r.navigate(WeeklyPlanningRoute)
    }
}