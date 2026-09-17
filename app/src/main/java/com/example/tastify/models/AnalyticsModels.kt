package com.example.tastify.models

data class DailyMetric(
    val timestamp: Long = 0L,
    val views: Int = 0,
    val likes: Int = 0,
    val comments: Int = 0
)

data class CompleteProfileAnalytics(
    val totalViews: Int,
    val totalLikes: Int,
    val totalComments: Int,
    val recipesCount: Int,
    val topRecipes: List<Recipe>,
    val weeklyTrend: List<Float>
)