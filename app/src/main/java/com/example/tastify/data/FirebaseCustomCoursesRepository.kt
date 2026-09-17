package com.example.tastify.data

import com.example.tastify.models.CustomCourse
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseCustomCoursesRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : CustomCoursesRepository {

    private fun recipeCollections(userId: String) = firestore
        .collection("users")
        .document(userId)
        .collection("recipe_collections")

    private fun courseDocument(userId: String, courseId: String) = recipeCollections(userId)
        .document(courseId)

    private fun courseRecipesCollection(userId: String, courseId: String) = courseDocument(userId, courseId)
        .collection("recipes")

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun getCustomCourses(userId: String): Flow<List<CustomCourse>> {
        return recipeCollections(userId)
            .whereEqualTo("type", COLLECTION_TYPE_CUSTOM)
            .snapshots()
            .flatMapLatest { snapshot ->
                val courseFlows = snapshot.documents.map { document ->
                    courseRecipesCollection(userId, document.id)
                        .orderBy("addedAt", Query.Direction.ASCENDING)
                        .snapshots()
                        .map { recipesSnapshot ->
                            CustomCourse(
                                id = document.getString("id") ?: document.id,
                                title = document.getString("title").orEmpty(),
                                author = document.getString("author").orEmpty(),
                                recipeIds = recipesSnapshot.documents.map { recipeDocument ->
                                    recipeDocument.getString("recipeId") ?: recipeDocument.id
                                }
                            )
                        }
                }

                if (courseFlows.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(courseFlows) { courses -> courses.toList() }
                }
            }
    }

    override suspend fun createCourse(userId: String, title: String) {
        val newDocRef = recipeCollections(userId).document()
        val course = mapOf(
            "id" to newDocRef.id,
            "title" to title,
            "author" to userId,
            "type" to COLLECTION_TYPE_CUSTOM,
            "createdAt" to System.currentTimeMillis()
        )
        newDocRef.set(course).await()
    }

    override suspend fun deleteCourse(userId: String, courseId: String) {
        val recipesSnapshot = courseRecipesCollection(userId, courseId).get().await()
        val batch = firestore.batch()
        recipesSnapshot.documents.forEach { document ->
            batch.delete(document.reference)
        }
        batch.delete(courseDocument(userId, courseId))
        batch.commit().await()
    }

    override suspend fun addRecipeToCourse(userId: String, courseId: String, recipeId: String) {
        courseRecipesCollection(userId, courseId)
            .document(recipeId)
            .set(
                mapOf(
                    "recipeId" to recipeId,
                    "addedAt" to System.currentTimeMillis()
                )
            )
            .await()
    }

    override suspend fun removeRecipeFromCourse(userId: String, courseId: String, recipeId: String) {
        courseRecipesCollection(userId, courseId)
            .document(recipeId)
            .delete()
            .await()
    }

    private companion object {
        const val COLLECTION_TYPE_CUSTOM = "custom"
    }
}
