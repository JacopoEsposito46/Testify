package com.example.tastify.data

import com.example.tastify.models.RecipeDailyAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRecipeAnalyticsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : RecipeAnalyticsRepository {

    private fun analyticsCollection(recipeId: String) = firestore
        .collection("recipes")
        .document(recipeId)
        .collection("analytics")

    override fun getDailyAnalytics(recipeId: String, limit: Int): Flow<List<RecipeDailyAnalytics>> {
        return analyticsCollection(recipeId)
            .orderBy(DATE_FIELD, Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .snapshots()
            .map { snapshot -> snapshot.toObjects<RecipeDailyAnalytics>() }
    }

    private companion object {
        const val DATE_FIELD = "date"
    }
}