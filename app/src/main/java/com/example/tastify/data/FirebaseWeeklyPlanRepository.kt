package com.example.tastify.data

import com.example.tastify.models.PlannedMeal
import com.example.tastify.models.UserWeeklyPlan
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseWeeklyPlanRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : WeeklyPlanRepository {

    private fun planDocRef(userId: String) =
        firestore.collection("users").document(userId)
            .collection("weekly_plans").document("current")

    override fun getUserPlan(userId: String): Flow<UserWeeklyPlan?> {
        if (userId.isBlank() || userId == "-1") {
            return flowOf(null)
        }
        
        return planDocRef(userId).snapshots().map { snapshot ->
            if (snapshot.exists()) {
                snapshot.toObject(UserWeeklyPlan::class.java)
            } else {
                UserWeeklyPlan(userId = userId)
            }
        }
    }

    override suspend fun addMealToPlan(userId: String, meal: PlannedMeal) {
        if (userId.isBlank() || userId == "-1") return
        val docRef = planDocRef(userId)
        
        val snapshot = docRef.get().await()
        if (snapshot.exists()) {
            val plan = snapshot.toObject(UserWeeklyPlan::class.java)
            if (plan != null) {
                val updatedMeals = plan.plannedMeals + meal
                docRef.update("plannedMeals", updatedMeals).await()
            }
        } else {
            val newPlan = UserWeeklyPlan(userId = userId, plannedMeals = listOf(meal))
            docRef.set(newPlan).await()
        }
    }

    override suspend fun removeMealFromPlan(
        userId: String,
        date: String,
        mealType: String,
        recipeId: String
    ) {
        if (userId.isBlank() || userId == "-1") return
        val docRef = planDocRef(userId)
        val snapshot = docRef.get().await()
        if (snapshot.exists()) {
            val plan = snapshot.toObject(UserWeeklyPlan::class.java)
            if (plan != null) {
                val indexToRemove = plan.plannedMeals.indexOfFirst { 
                    it.date == date && it.mealType == mealType && it.recipeId == recipeId 
                }
                if (indexToRemove != -1) {
                    val updatedMeals = plan.plannedMeals.toMutableList()
                    updatedMeals.removeAt(indexToRemove)
                    docRef.update("plannedMeals", updatedMeals).await()
                }
            }
        }
    }
}
