package com.example.tastify.models

data class RecipeDailyAnalytics(
    val date: String = "",
    val views: Int = 0,
    val favoritesAdded: Int = 0,
    val favoritesRemoved: Int = 0,
    val comments: Int = 0,
    val commentsRemoved: Int = 0,
    val reviews: Int = 0,
    val reviewsRemoved: Int = 0
)