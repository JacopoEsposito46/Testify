package com.example.tastify.data

import android.content.Context
import com.example.tastify.models.Review
import com.example.tastify.utils.toSupabasePublicUrl
import com.example.tastify.utils.uploadImageToSupabaseStorage
import com.example.tastify.utils.withCacheBuster
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseReviewRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val supabase: SupabaseClient,
    private val firestore: FirebaseFirestore
) : ReviewRepository {

    private companion object {
        const val TAG = "FirebaseReviewRepository"
        const val AVATAR_BUCKET = "avatars"
        const val REVIEW_PHOTO_BUCKET = "recipe_review_photo"
        const val REVIEW_COUNT_FIELD = "reviewCount"
        const val ANALYTICS_COLLECTION = "analytics"
        const val DATE_PATTERN = "yyyy-MM-dd"
        const val DATE_FIELD = "date"
        const val REVIEWS_FIELD = "reviews"
        const val REVIEWS_REMOVED_FIELD = "reviewsRemoved"
        const val EXP_FOR_WRITTEN_REVIEW = 20
        const val EXP_FOR_RECEIVED_REVIEW = 15
    }

    override fun getReviewsForRecipe(recipeId: String): Flow<List<Review>> {
        return firestore.collection("recipes")
            .document(recipeId)
            .collection("reviews")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects<Review>().map { review ->
                    review.copy(
                        authorProfileImageUri = review.authorProfileImageUri.toSupabasePublicUrl(
                            supabase = supabase,
                            bucket = AVATAR_BUCKET
                        ).withCacheBuster(),
                        imageUris = review.imageUris.map { imageUri ->
                            imageUri.toSupabasePublicUrl(
                                supabase = supabase,
                                bucket = REVIEW_PHOTO_BUCKET
                            )
                        }
                    )
                }
            }
    }


    override suspend fun addReview(review: Review) {
        val reviewWithUploadedImages = review.uploadLocalImages()
        val recipeRef = firestore.collection("recipes").document(reviewWithUploadedImages.recipeId)
        val reviewRef = recipeRef.collection("reviews").document(reviewWithUploadedImages.reviewId)
        val date = todayKey()
        val analyticsRef = recipeRef.collection(ANALYTICS_COLLECTION).document(date)

        firestore.runTransaction { transaction ->
            val reviewSnapshot = transaction.get(reviewRef)
            if (reviewSnapshot.exists()) {
                return@runTransaction null
            }

            val recipeSnapshot = transaction.get(recipeRef)
            val recipeAuthorId = recipeSnapshot.getString("authorId")

            transaction.set(reviewRef, reviewWithUploadedImages)
            transaction.update(recipeRef, REVIEW_COUNT_FIELD, FieldValue.increment(1L))
            transaction.update(
                firestore.collection("users").document(review.authorId),
                "experiencePoints",
                FieldValue.increment(EXP_FOR_WRITTEN_REVIEW.toLong())
            )
            if (recipeAuthorId != null && recipeAuthorId != review.authorId) {
                transaction.update(
                    firestore.collection("users").document(recipeAuthorId),
                    "experiencePoints",
                    FieldValue.increment(EXP_FOR_RECEIVED_REVIEW.toLong())
                )
            }
            transaction.set(
                analyticsRef,
                mapOf(
                    DATE_FIELD to date,
                    REVIEWS_FIELD to FieldValue.increment(1L)
                ),
                SetOptions.merge()
            )
            null
        }.await()
    }
    override suspend fun deleteReview(recipeId: String, reviewId: String) {
        val recipeRef = firestore.collection("recipes").document(recipeId)
        val reviewRef = recipeRef.collection("reviews").document(reviewId)
        val date = todayKey()
        val analyticsRef = recipeRef.collection(ANALYTICS_COLLECTION).document(date)

        firestore.runTransaction { transaction ->
            val reviewSnapshot = transaction.get(reviewRef)
            if (reviewSnapshot.exists()) {
                transaction.delete(reviewRef)
                transaction.update(recipeRef, REVIEW_COUNT_FIELD, FieldValue.increment(-1L))
                transaction.set(
                    analyticsRef,
                    mapOf(
                        DATE_FIELD to date,
                        REVIEWS_REMOVED_FIELD to FieldValue.increment(1L)
                    ),
                    SetOptions.merge()
                )
            }
            null
        }.await()
    }

    override suspend fun addReplyToReview(recipeId: String, reviewId: String, replyText: String?) {
        val updateValue = replyText ?: FieldValue.delete()
        firestore.collection("recipes")
            .document(recipeId)
            .collection("reviews")
            .document(reviewId)
            .update("authorReplyText", updateValue)
            .await()
    }

    private fun todayKey(): String =
        SimpleDateFormat(DATE_PATTERN, Locale.US).format(Date())

    private suspend fun Review.uploadLocalImages(): Review {
        val uploadedImageUris = imageUris.mapIndexed { index, imageUri ->
            imageStorageReferenceForSave(
                imageUri = imageUri,
                storagePath = "$recipeId/$reviewId/image-${index + 1}.jpg"
            )
        }

        return copy(imageUris = uploadedImageUris)
    }

    private suspend fun imageStorageReferenceForSave(
        imageUri: String,
        storagePath: String
    ): String {
        return uploadImageToSupabaseStorage(
            context = context,
            supabase = supabase,
            bucket = REVIEW_PHOTO_BUCKET,
            imageUri = imageUri,
            storagePath = storagePath,
            logTag = TAG
        )
    }
}
