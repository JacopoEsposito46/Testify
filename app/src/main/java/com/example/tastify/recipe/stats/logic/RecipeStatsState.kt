package com.example.tastify.recipe.stats.logic

data class RecipeStatsState(
    val isLoading: Boolean = true,
    val recipeTitle: String = "",
    val totalViews: Int = 0,
    val totalFavorites: Int = 0,
    val totalComments: Int = 0,
    val totalReviews: Int = 0,
    val averageRating: Double = 0.0,
    val viewsChangeText: String = "+0% vs yesterday",
    val favoritesChangeText: String = "+0% vs yesterday",
    val commentsChangeText: String = "+0% vs yesterday",
    val reviewsChangeText: String = "+0% vs yesterday",
    val selectedPeriod: RecipeStatsPeriod = RecipeStatsPeriod.WEEK,
    val trendPoints: List<RecipeTrendPoint> = emptyList()
)

enum class RecipeStatsPeriod {
    WEEK,
    MONTH
}

data class RecipeTrendPoint(
    val label: String,
    val value: Int
)
