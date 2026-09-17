package com.example.tastify.recipe.courses.logic

sealed interface CustomCoursesEvent {
    data class CreateCourse(val title: String) : CustomCoursesEvent
    data class DeleteCourse(val courseId: String) : CustomCoursesEvent
    data class AddRecipeToCourse(val courseId: String, val recipeId: String) : CustomCoursesEvent
    data class RemoveRecipeFromCourse(val courseId: String, val recipeId: String) : CustomCoursesEvent
    data class ToggleFavourite(val recipeId: String) : CustomCoursesEvent
    data object Refresh : CustomCoursesEvent
}