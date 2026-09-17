package com.example.tastify.data

import android.content.Context
import com.example.tastify.models.CuisineType
import com.example.tastify.models.DietaryRestriction
import com.example.tastify.models.Recipe
import com.example.tastify.models.RecipeDifficulty
import com.example.tastify.utils.SessionManager
import com.example.tastify.utils.toSupabasePublicUrl
import com.example.tastify.utils.uploadImageToSupabaseStorage
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRecipeRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val supabase: SupabaseClient,
    private val firestore: FirebaseFirestore
) : RecipeRepository {

    private companion object {
        const val TAG = "FirebaseRecipeRepository"
        const val RECIPES_BUCKET = "recipes"
        const val STEPS_BUCKET = "steps"
        const val ANALYTICS_COLLECTION = "analytics"
        const val DATE_PATTERN = "yyyy-MM-dd"
        const val DATE_FIELD = "date"
        const val VIEWS_FIELD = "views"
    }

    private val recipesCollection = firestore.collection("recipes")

    override fun getAllRecipes(): Flow<List<Recipe>> {
        val currentUserId = SessionManager.CURRENT_LOGGED_IN_USER_ID ?: ""
        return recipesCollection.where(
            Filter.or(
                Filter.equalTo("isPublic", true),
                Filter.equalTo("authorId", currentUserId)
            )
        )
            .snapshots().map { snapshot ->
            snapshot.toObjects<Recipe>().map { it.withPublicImageUrls() }
        /*return recipesCollection
            .snapshots().map { snapshot ->
            snapshot.toObjects<Recipe>()
                .filter { it.isPublic }
                .map { it.withPublicImageUrls() }*/
        }
    }

    override fun getRecipeByOwner(ownerId: String): Flow<List<Recipe>> {
        return recipesCollection.whereEqualTo("authorId", ownerId)
            .snapshots()
            .map { snapshot -> snapshot.toObjects<Recipe>().map { it.withPublicImageUrls() } }
    }

    override fun getImportedRecipesByOwner(ownerId: String): Flow<List<Recipe>> {
        return recipesCollection
            .whereEqualTo("authorId", ownerId)
            .whereEqualTo("source", Recipe.SOURCE_IMPORTED)
            .snapshots()
            .map { snapshot -> snapshot.toObjects<Recipe>().map { it.withPublicImageUrls() } }
    }

    override fun getFilteredRecipes(
        searchQuery: String,
        difficulty: RecipeDifficulty?,
        cuisineTypes: Set<CuisineType>,
        dietaryRestrictions: Set<DietaryRestriction>,
        maxCalories: Float
    ): Flow<List<Recipe>> {

        val currentUserId = SessionManager.CURRENT_LOGGED_IN_USER_ID ?: ""

        var query: Query = recipesCollection.where(
            Filter.or(
                Filter.equalTo("isPublic", true),
                Filter.equalTo("authorId", currentUserId)
            )
        )

        if (difficulty != null) {
            query = query.whereEqualTo("difficulty", difficulty.name)
        }

        if (dietaryRestrictions.isNotEmpty()) {
            query = query.whereArrayContains("dietaryRestrictions", dietaryRestrictions.first().name)
        }

        return query.snapshots().map { snapshot ->
            var recipesList = snapshot.toObjects<Recipe>()

            if (cuisineTypes.isNotEmpty()) {
                recipesList = recipesList.filter { cuisineTypes.contains(it.cuisineType) }
            }
            if (maxCalories < 4000f) {
                recipesList = recipesList.filter { it.calories <= maxCalories }
            }

            recipesList.map { it.withPublicImageUrls() }
        }
    }

    override suspend fun addRecipe(recipe: Recipe) {
        val recipeWithUploadedImages = recipe.uploadLocalImages()
        val recipeRef = recipesCollection.document(recipeWithUploadedImages.recipeId)
        firestore.runTransaction { transaction ->
            val alreadyExists = transaction.get(recipeRef).exists()
            transaction.set(recipeRef, recipeWithUploadedImages)

            if (!alreadyExists && recipeWithUploadedImages.authorId.isNotBlank() && recipeWithUploadedImages.isPublic) {
                transaction.update(
                    firestore.collection("users").document(recipeWithUploadedImages.authorId),
                    "postedRecipes",
                    FieldValue.increment(1)
                )
            }
            null
        }.await()
    }

    override suspend fun updateRecipe(recipe: Recipe) {
        val recipeWithUploadedImages = recipe.uploadLocalImages()
        val recipeRef = recipesCollection.document(recipeWithUploadedImages.recipeId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(recipeRef)
            val wasPublic = snapshot.getBoolean("isPublic") ?: true

            transaction.set(recipeRef, recipeWithUploadedImages)

            if (recipeWithUploadedImages.authorId.isNotBlank() && wasPublic != recipeWithUploadedImages.isPublic) {
                transaction.update(
                    firestore.collection("users").document(recipeWithUploadedImages.authorId),
                    "postedRecipes",
                    FieldValue.increment(if (recipeWithUploadedImages.isPublic) 1L else -1L)
                )
            }
            null
        }.await()
    }

    override suspend fun deleteRecipe(recipeId: String) {
        val recipeRef = recipesCollection.document(recipeId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(recipeRef)
            val authorId = snapshot.getString("authorId").orEmpty()
            val isPublic = snapshot.getBoolean("isPublic") ?: true

            if (snapshot.exists()) {
                transaction.delete(recipeRef)

                if (authorId.isNotBlank() && isPublic) {
                    transaction.update(
                        firestore.collection("users").document(authorId),
                        "postedRecipes",
                        FieldValue.increment(-1)
                    )
                }
            }
            null
        }.await()
    }

    override suspend fun updateRecipeVisibility(recipeId: String, isPublic: Boolean) {
        val recipeRef = recipesCollection.document(recipeId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(recipeRef)
            val authorId = snapshot.getString("authorId").orEmpty()
            val wasPublic = snapshot.getBoolean("isPublic") ?: true
            val reportCount = snapshot.getLong("reportCount")?.toInt() ?: 0

            if (reportCount < 2) {
                transaction.update(recipeRef, "isPublic", isPublic)

                if (authorId.isNotBlank() && wasPublic != isPublic) {
                    transaction.update(
                        firestore.collection("users").document(authorId),
                        "postedRecipes",
                        FieldValue.increment(if (isPublic) 1L else -1L)
                    )
                }
            } else {
                throw IllegalStateException("Recipe has too many reports to be public")
            }
            null
        }.await()
    }

    override suspend fun updateRecipeRating(recipeId: String, newRating: Double) {
        recipesCollection.document(recipeId).update("rating", newRating).await()
    }

    override fun getRecipeById(recipeId: String): Flow<Recipe?> {
        return recipesCollection.document(recipeId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) snapshot.toObject<Recipe>()?.withPublicImageUrls() else null
            }
    }

    override fun getRecipesByIds(recipeIds: List<String>): Flow<List<Recipe>> {
        if (recipeIds.isEmpty()) return flowOf(emptyList())

        val chunks = recipeIds.chunked(30)

        val flows = chunks.map { chunk ->
            recipesCollection.whereEqualTo("isPublic", true).whereIn("recipeId", chunk)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects<Recipe>()
                        .map { it.withPublicImageUrls() }
                }
        }
        return combine(flows) { arrays: Array<List<Recipe>> ->
            arrays.flatMap { it }
        }
    }
    override suspend fun incrementViews(recipeId: String) {
        val recipeRef = recipesCollection.document(recipeId)
        val date = todayKey()
        val analyticsRef = recipeRef.collection(ANALYTICS_COLLECTION).document(date)

        firestore.runTransaction { transaction ->
            transaction.update(recipeRef, VIEWS_FIELD, FieldValue.increment(1L))
            transaction.set(
                analyticsRef,
                mapOf(
                    DATE_FIELD to date,
                    VIEWS_FIELD to FieldValue.increment(1L)
                ),
                SetOptions.merge()
            )
            null
        }.await()
    }

    private fun todayKey(): String =
        SimpleDateFormat(DATE_PATTERN, Locale.US).format(Date())

    private suspend fun Recipe.uploadLocalImages(): Recipe {
        val coverPhotoPath = imageStorageReferenceForSave(
            bucket = RECIPES_BUCKET,
            imageUri = recipePhotoId,
            storagePath = "$recipeId/cover.jpg"
        )

        val uploadedSteps = steps.mapIndexed { index, step ->
            val stepPhotoPath = imageStorageReferenceForSave(
                bucket = STEPS_BUCKET,
                imageUri = step.stepPhotoId,
                storagePath = "$recipeId/step-${step.stepNumber.takeIf { it > 0 } ?: (index + 1)}.jpg"
            )
            step.copy(stepPhotoId = stepPhotoPath)
        }

        return copy(
            recipePhotoId = coverPhotoPath,
            steps = uploadedSteps
        )
    }

    private suspend fun imageStorageReferenceForSave(
        bucket: String,
        imageUri: String,
        storagePath: String
    ): String {
        return uploadImageToSupabaseStorage(
            context = context,
            supabase = supabase,
            bucket = bucket,
            imageUri = imageUri,
            storagePath = storagePath,
            logTag = TAG
        )
    }

    private fun Recipe.withPublicImageUrls(): Recipe {
        return copy(
            recipePhotoId = recipePhotoId.toPublicStorageUrl(RECIPES_BUCKET),
            steps = steps.map { step ->
                step.copy(stepPhotoId = step.stepPhotoId.toPublicStorageUrl(STEPS_BUCKET))
            }
        )
    }

    private fun String.toPublicStorageUrl(bucket: String): String {
        return toSupabasePublicUrl(supabase, bucket)
    }
}
