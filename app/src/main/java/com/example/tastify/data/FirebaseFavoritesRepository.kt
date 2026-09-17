package com.example.tastify.data

import com.example.tastify.models.FavoriteReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFavoritesRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : FavoritesRepository {

    private fun recipeCollections(userId: String) = firestore
        .collection("users")
        .document(userId)
        .collection("recipe_collections")

    private fun favoritesDocument(userId: String) = recipeCollections(userId)
        .document(FAVORITES_COLLECTION_ID)

    private fun favoriteRecipesCollection(userId: String) = favoritesDocument(userId)
        .collection("recipes")

    private fun recipeDocument(recipeId: String) = firestore.collection("recipes")
        .document(recipeId)

    override fun isRecipeFavorite(userId: String, recipeId: String): Flow<Boolean> {
        return favoriteRecipesCollection(userId)
            .document(recipeId)
            .snapshots()
            .map { snapshot -> snapshot.exists() }
    }

    override fun getFavoriteRecipesReferences(userId: String, limit: Int): Flow<List<FavoriteReference>> {
        return favoriteRecipesCollection(userId)
            .orderBy("addedAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .snapshots()
            .map { snapshot -> snapshot.toObjects<FavoriteReference>() }
    }

    override suspend fun addRecipeToFavorites(userId: String, recipeId: String) {
        val favoritesDoc = favoritesDocument(userId)
        val favoriteRecipeDoc = favoriteRecipesCollection(userId).document(recipeId)
        val recipeDoc = recipeDocument(recipeId)
        val date = todayKey()
        val analyticsDoc = recipeDoc.collection(ANALYTICS_COLLECTION).document(date)

        firestore.runTransaction { transaction ->
            val favoriteSnapshot = transaction.get(favoriteRecipeDoc)

            transaction.set(
                favoritesDoc,
                mapOf(
                    "id" to FAVORITES_COLLECTION_ID,
                    "title" to "Favorites",
                    "type" to COLLECTION_TYPE_FAVORITES
                ),
                SetOptions.merge()
            )

            if (!favoriteSnapshot.exists()) {
                transaction.set(
                    favoriteRecipeDoc,
                    mapOf(
                        "recipeId" to recipeId,
                        "addedAt" to System.currentTimeMillis()
                    )
                )
                transaction.update(recipeDoc, FAVORITE_COUNT_FIELD, FieldValue.increment(1L))
                transaction.set(
                    analyticsDoc,
                    mapOf(
                        DATE_FIELD to date,
                        FAVORITES_ADDED_FIELD to FieldValue.increment(1L)
                    ),
                    SetOptions.merge()
                )
            }

            null
        }.await()
    }

    override suspend fun removeRecipeFromFavorites(userId: String, recipeId: String) {
        val favoriteRecipeDoc = favoriteRecipesCollection(userId).document(recipeId)
        val recipeDoc = recipeDocument(recipeId)
        val date = todayKey()
        val analyticsDoc = recipeDoc.collection(ANALYTICS_COLLECTION).document(date)

        firestore.runTransaction { transaction ->
            val favoriteSnapshot = transaction.get(favoriteRecipeDoc)

            if (favoriteSnapshot.exists()) {
                transaction.delete(favoriteRecipeDoc)
                transaction.update(recipeDoc, FAVORITE_COUNT_FIELD, FieldValue.increment(-1L))
                transaction.set(
                    analyticsDoc,
                    mapOf(
                        DATE_FIELD to date,
                        FAVORITES_REMOVED_FIELD to FieldValue.increment(1L)
                    ),
                    SetOptions.merge()
                )
            }

            null
        }.await()
    }

    private fun todayKey(): String =
        SimpleDateFormat(DATE_PATTERN, Locale.US).format(Date())

    private companion object {
        const val FAVORITES_COLLECTION_ID = "favorites"
        const val COLLECTION_TYPE_FAVORITES = "favorites"
        const val FAVORITE_COUNT_FIELD = "favouriteCount"
        const val ANALYTICS_COLLECTION = "analytics"
        const val DATE_PATTERN = "yyyy-MM-dd"
        const val DATE_FIELD = "date"
        const val FAVORITES_ADDED_FIELD = "favoritesAdded"
        const val FAVORITES_REMOVED_FIELD = "favoritesRemoved"
    }
}
