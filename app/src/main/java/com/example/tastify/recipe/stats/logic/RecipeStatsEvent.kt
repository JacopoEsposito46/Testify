package com.example.tastify.recipe.stats.logic

sealed interface RecipeStatsEvent {
    data class ChangePeriod(val period: RecipeStatsPeriod) : RecipeStatsEvent
}