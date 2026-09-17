package com.example.tastify.data

import com.example.tastify.models.Report
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseReportRepository @Inject constructor(
    private val firestore: FirebaseFirestore
): ReportRepository {

    override fun getReportsForRecipe(recipeId: String): Flow<List<Report>> {
        return firestore.collection("recipes")
            .document(recipeId)
            .collection("reports")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects<Report>()
            }
    }

    override suspend fun addReport(report: Report) {
        val recipeRef = firestore.collection("recipes").document(report.recipeId)
        val reportRef = recipeRef.collection("reports").document(report.authorId)

        firestore.runTransaction { transaction ->

            val recipeSnapshot = transaction.get(recipeRef)
            val reportSnapshot = transaction.get(reportRef)

            if (reportSnapshot.exists()) {
                transaction.set(reportRef, report)
                return@runTransaction null
            }

            val currentReportCount = recipeSnapshot.getLong("reportCount")?.toInt() ?: 0
            val newReportCount = currentReportCount + 1
            val wasPublic = recipeSnapshot.getBoolean("isPublic") ?: true
            val authorId = recipeSnapshot.getString("authorId").orEmpty()

            transaction.set(reportRef, report)

            val recipeUpdates = mutableMapOf<String, Any>(
                "reportCount" to newReportCount
            )

            if (newReportCount >= 2) {
                recipeUpdates["isPublic"] = false
            }

            transaction.update(recipeRef, recipeUpdates)

            if (newReportCount >= 2 && wasPublic && authorId.isNotBlank()) {
                transaction.update(
                    firestore.collection("users").document(authorId),
                    "postedRecipes",
                    FieldValue.increment(-1L)
                )
            }
            null
        }.await()
    }
}