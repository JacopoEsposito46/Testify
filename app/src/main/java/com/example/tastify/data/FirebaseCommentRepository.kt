package com.example.tastify.data

import com.example.tastify.models.Comment
import com.example.tastify.utils.toSupabasePublicUrl
import com.example.tastify.utils.withCacheBuster
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
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
class FirebaseCommentRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val firestore: FirebaseFirestore
) : CommentRepository {

    private companion object {
        const val AVATAR_BUCKET = "avatars"
        const val COMMENT_COUNT_FIELD = "commentCount"
        const val ANALYTICS_COLLECTION = "analytics"
        const val DATE_PATTERN = "yyyy-MM-dd"
        const val DATE_FIELD = "date"
        const val COMMENTS_FIELD = "comments"
        const val COMMENTS_REMOVED_FIELD = "commentsRemoved"
        const val EXP_FOR_WRITTEN_COMMENT = 10
        const val EXP_FOR_RECEIVED_COMMENT = 5
    }

    override fun getCommentsForRecipe(recipeId: String): Flow<List<Comment>> {
        return firestore.collection("recipes")
            .document(recipeId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects<Comment>().map { comment ->
                    comment.copy(
                        authorProfileImageUri = comment.authorProfileImageUri.toSupabasePublicUrl(
                            supabase = supabase,
                            bucket = AVATAR_BUCKET
                        ).withCacheBuster()
                    )
                }
            }
    }


    override suspend fun addComment(comment: Comment) {
        val recipeRef = firestore.collection("recipes").document(comment.recipeId)
        val commentRef = recipeRef.collection("comments").document(comment.commentId)
        val date = todayKey()
        val analyticsRef = recipeRef.collection(ANALYTICS_COLLECTION).document(date)

        firestore.runTransaction { transaction ->
            val commentSnapshot = transaction.get(commentRef)
            if (commentSnapshot.exists()) {
                return@runTransaction null
            }
            val recipeSnapshot = transaction.get(recipeRef)
            val recipeAuthorId = recipeSnapshot.getString("authorId")

            transaction.set(commentRef, comment)
            transaction.update(recipeRef, COMMENT_COUNT_FIELD, FieldValue.increment(1L))
            transaction.update(
                firestore.collection("users").document(comment.authorId),
                "experiencePoints",
                FieldValue.increment(EXP_FOR_WRITTEN_COMMENT.toLong())
            )
            if (recipeAuthorId != null && recipeAuthorId != comment.authorId) {
                transaction.update(
                    firestore.collection("users").document(recipeAuthorId),
                    "experiencePoints",
                    FieldValue.increment(EXP_FOR_RECEIVED_COMMENT.toLong())
                )
            }
            transaction.set(
                analyticsRef,
                mapOf(
                    DATE_FIELD to date,
                    COMMENTS_FIELD to FieldValue.increment(1L)
                ),
                SetOptions.merge()
            )
            null
        }.await()
    }


    override suspend fun deleteComment(recipeId: String, commentId: String) {
        val recipeRef = firestore.collection("recipes").document(recipeId)
        val commentRef = recipeRef.collection("comments").document(commentId)
        val date = todayKey()
        val analyticsRef = recipeRef.collection(ANALYTICS_COLLECTION).document(date)

        firestore.runTransaction { transaction ->
            val commentSnapshot = transaction.get(commentRef)
            if (commentSnapshot.exists()) {
                transaction.delete(commentRef)
                transaction.update(recipeRef, COMMENT_COUNT_FIELD, FieldValue.increment(-1L))
                transaction.set(
                    analyticsRef,
                    mapOf(
                        DATE_FIELD to date,
                        COMMENTS_REMOVED_FIELD to FieldValue.increment(1L)
                    ),
                    SetOptions.merge()
                )
            }
            null
        }.await()
    }

    private fun todayKey(): String =
        SimpleDateFormat(DATE_PATTERN, Locale.US).format(Date())


    override suspend fun toggleLike(recipeId: String, commentId: String, userId: String) {
        val docRef = firestore.collection("recipes")
            .document(recipeId)
            .collection("comments")
            .document(commentId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)

            if (snapshot.exists()) {
                val likedBy = snapshot.get("likedBy") as? List<*> ?: emptyList<Any>()

                if (likedBy.contains(userId)) {
                    transaction.update(docRef, "likedBy", FieldValue.arrayRemove(userId))
                } else {
                    transaction.update(docRef, "likedBy", FieldValue.arrayUnion(userId))
                }
            }
            null
        }.await()
    }


    override suspend fun unlikeComment(recipeId: String, commentId: String, userId: String) {
        firestore.collection("recipes")
            .document(recipeId)
            .collection("comments")
            .document(commentId)
            .update("likedBy", FieldValue.arrayRemove(userId))
            .await()
    }
}
