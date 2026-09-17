package com.example.tastify.data

import com.example.tastify.models.CustomCourse
import kotlinx.coroutines.flow.Flow

interface CustomCoursesRepository {
    fun getCustomCourses(userId: String): Flow<List<CustomCourse>>
    suspend fun createCourse(userId: String, title: String)
    suspend fun addRecipeToCourse(userId: String, courseId: String, recipeId: String)
    suspend fun removeRecipeFromCourse(userId: String, courseId: String, recipeId: String)
    suspend fun deleteCourse(userId: String, courseId: String)
}