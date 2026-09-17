package com.example.tastify.data

import com.example.tastify.models.Ingredient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseIngredientRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : IngredientRepository {

    private val ingredientsCollection = firestore.collection("ingredients")

    override fun getIngredientsFlow(): Flow<List<Ingredient>> {
        return ingredientsCollection
            .orderBy("name", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects<Ingredient>() }
    }

    override suspend fun searchIngredients(query: String): List<Ingredient> {
        if (query.isBlank()) return emptyList()

        val targetQuery = query.trim()
        val querySnapshot = ingredientsCollection
            .whereGreaterThanOrEqualTo("name", targetQuery)
            .whereLessThanOrEqualTo("name", targetQuery + "\uf8ff")
            .get()
            .await()

        return querySnapshot.toObjects<Ingredient>()
    }

    override suspend fun getIngredientById(id: String): Ingredient? {
        val snapshot = ingredientsCollection.document(id).get().await()
        return if (snapshot.exists()) snapshot.toObject<Ingredient>() else null
    }

    override suspend fun addIngredient(ingredient: Ingredient) {
        ingredientsCollection.document(ingredient.ingredientId)
            .set(ingredient)
            .await()
    }
}