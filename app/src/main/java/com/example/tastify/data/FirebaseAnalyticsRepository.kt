package com.example.tastify.data

import android.util.Log
import com.example.tastify.models.CompleteProfileAnalytics
import com.example.tastify.models.DailyMetric
import com.example.tastify.models.Recipe
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class FirebaseAnalyticsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : AnalyticsRepository {

    override fun getProfileAnalytics(userId: String): Flow<CompleteProfileAnalytics> = callbackFlow {
        val recipesListener = firestore.collection("recipes")
            .whereEqualTo("authorId", userId)
            .addSnapshotListener { recipesSnapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val recipes = recipesSnapshot?.toObjects(Recipe::class.java) ?: emptyList()

                val totalViews = recipes.sumOf { it.views }
                val totalLikes = recipes.sumOf { it.favouriteCount }
                val recipesCount = recipes.size
                val topRecipes = recipes.sortedByDescending { it.views }.take(3)

                val sevenDaysAgo = System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000)

                firestore.collection("users")
                    .document(userId)
                    .collection("daily_analytics")
                    .whereGreaterThanOrEqualTo("timestamp", sevenDaysAgo)
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener { trendSnapshot ->
                        val metrics = trendSnapshot.toObjects(DailyMetric::class.java)

                        val totalComments = metrics.sumOf { it.comments }
                        val trendValues = FloatArray(7) { 0f }
                        val calendar = Calendar.getInstance()

                        for (metric in metrics) {
                            calendar.timeInMillis = metric.timestamp
                            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                            val index = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - 2
                            if (index in 0..6) {
                                trendValues[index] += metric.views.toFloat()
                            }
                        }

                        val max = trendValues.maxOrNull() ?: 1f
                        val safeMax = if (max > 0f) max else 1f
                        val normalizedTrend = trendValues.map { it / safeMax }

                        trySend(
                            CompleteProfileAnalytics(
                                totalViews = totalViews,
                                totalLikes = totalLikes,
                                totalComments = totalComments,
                                recipesCount = recipesCount,
                                topRecipes = topRecipes,
                                weeklyTrend = normalizedTrend
                            )
                        )
                    }
                    .addOnFailureListener { exception ->
                        close(exception)
                    }
            }

        awaitClose { recipesListener.remove() }
    }

    override suspend fun trackRecipeView(userId: String, recipeId: String) {
        try {
            val dateString = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val dailyDocRef = firestore.collection("users")
                .document(userId)
                .collection("daily_analytics")
                .document(dateString)

            val dailyUpdates = hashMapOf(
                "views" to FieldValue.increment(1),
                "timestamp" to System.currentTimeMillis()
            )

            dailyDocRef.set(dailyUpdates, SetOptions.merge()).await()

            val recipeRef = firestore.collection("recipes").document(recipeId)
            val recipeUpdates = mapOf(
                "views" to FieldValue.increment(1)
            )

            recipeRef.update(recipeUpdates).await()
        } catch (e: Exception) {
            Log.e("FirebaseAnalytics", e.message.toString())
        }
    }

    override suspend fun trackRecipeLike(userId: String, recipeId: String, isLike: Boolean) {
        try {
            val dateString = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val dailyDocRef = firestore.collection("users")
                .document(userId)
                .collection("daily_analytics")
                .document(dateString)

            val incrementValue = if (isLike) 1L else -1L

            val dailyUpdates = hashMapOf(
                "likes" to FieldValue.increment(incrementValue),
                "timestamp" to System.currentTimeMillis()
            )

            dailyDocRef.set(dailyUpdates, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("FirebaseAnalytics", e.message.toString())
        }
    }

    override suspend fun trackRecipeComment(userId: String, recipeId: String) {
        try {
            val dateString = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val dailyDocRef = firestore.collection("users")
                .document(userId)
                .collection("daily_analytics")
                .document(dateString)

            val dailyUpdates = hashMapOf(
                "comments" to FieldValue.increment(1),
                "timestamp" to System.currentTimeMillis()
            )

            dailyDocRef.set(dailyUpdates, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("FirebaseAnalytics", e.message.toString())
        }
    }
}