package com.example.tastify.data

import com.example.tastify.models.HistoryItem
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseHistoryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : HistoryRepository {

    private companion object {
        const val EXP_FOR_COOKED_RECIPE = 50
    }

    override fun getHistory(userId: String, limit: Int): Flow<List<HistoryItem>> {
        return historyCollection(userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects<HistoryItem>()
            }
    }

    override fun getDoneHistory(userId: String, limit: Int): Flow<List<HistoryItem>> {
        return doneHistoryCollection(userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects<HistoryItem>()
            }
    }

    override fun isRecipeDone(userId: String, recipeId: String): Flow<Boolean> {
        return doneHistoryCollection(userId)
            .document(recipeId)
            .snapshots()
            .map { snapshot -> snapshot.exists() }
    }

    override suspend fun addRecipeToHistory(userId: String, recipeId: String) {
        historyCollection(userId)
            .document(recipeId)
            .set(newHistoryItem(recipeId))
            .await()
    }

    override suspend fun removeRecipeFromHistory(userId: String, recipeId: String) {
        historyCollection(userId)
            .document(recipeId)
            .delete()
            .await()
    }

    override suspend fun addRecipeToDoneHistory(userId: String, recipeId: String) {
        val doneRef = doneHistoryCollection(userId).document(recipeId)
        val userRef = firestore.collection("users").document(userId)

        firestore.runTransaction { transaction ->
          val doneSnapshot = transaction.get(doneRef)
          if (!doneSnapshot.exists()) {
            transaction.set(doneRef, newHistoryItem(recipeId))
            transaction.update(userRef, "cookedRecipes", FieldValue.increment(1L))
            transaction.update(
              userRef,
              "experiencePoints",
              FieldValue.increment(EXP_FOR_COOKED_RECIPE.toLong())
            )
          }
          null
        }.await()
    }

    override suspend fun removeRecipeFromDoneHistory(userId: String, recipeId: String) {
        val doneRef = doneHistoryCollection(userId).document(recipeId)
        val userRef = firestore.collection("users").document(userId)

        firestore.runTransaction { transaction ->
            val doneSnapshot = transaction.get(doneRef)

            if (doneSnapshot.exists()) {
                transaction.delete(doneRef)
                transaction.update(userRef, "cookedRecipes", FieldValue.increment(-1L))
            }
            null
        }.await()
    }

    private fun historyCollection(userId: String) = firestore.collection("users")
        .document(userId)
        .collection("history")

    private fun doneHistoryCollection(userId: String) = firestore.collection("users")
        .document(userId)
        .collection("done_history")

    private fun newHistoryItem(recipeId: String) = HistoryItem(
        recipeId = recipeId,
        timestamp = System.currentTimeMillis()
    )
}