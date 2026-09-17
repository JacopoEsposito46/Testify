package com.example.tastify.domain

import com.example.tastify.data.CustomCoursesRepository
import com.example.tastify.models.CustomCourse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCoursesUseCase @Inject constructor(private val repository: CustomCoursesRepository) {
    operator fun invoke(userId: String): Flow<List<CustomCourse>> {
        return repository.getCustomCourses(userId)
    }
}

class CreateCourse @Inject constructor(private val repository: CustomCoursesRepository) {
    suspend operator fun invoke(userId: String, title: String){
        repository.createCourse(userId, title)
    }
}

class AddRecipeToCourse @Inject constructor(private val repository: CustomCoursesRepository) {
    suspend operator fun invoke(userId: String, courseId: String, recipeId: String) {
        repository.addRecipeToCourse(userId, courseId, recipeId)
    }
}

class RemoveRecipeFromCourse @Inject constructor(private val repository: CustomCoursesRepository) {
    suspend operator fun invoke(userId: String, courseId: String, recipeId: String) {
        repository.removeRecipeFromCourse(userId, courseId, recipeId)
    }
}

class DeleteCourse @Inject constructor(private val repository: CustomCoursesRepository) {
    suspend operator fun invoke(userId: String, courseId: String) {
        repository.deleteCourse(userId, courseId)
    }
}