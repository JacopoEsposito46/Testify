package com.example.tastify.data

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.example.tastify.models.ImageSource
import com.example.tastify.models.UserProfile
import com.example.tastify.utils.toSupabasePublicUrl
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseProfileRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val supabase: SupabaseClient,
    private val firestore: FirebaseFirestore
) : ProfileRepository {

    private companion object {
        const val AVATAR_BUCKET = "avatars"
    }
    private val usersCollection = firestore.collection("users")

    override fun getUserProfile(userId: String): Flow<UserProfile> {
        return usersCollection.document(userId)
            .snapshots()
            .map { snapshot ->
                val profile = snapshot.toObject<UserProfile>() ?: UserProfile(internalID = userId)
                val url = profile.profilePictureUrl
                if (!url.isNullOrBlank()) {
                    profile.profilePicture = ImageSource.Uri(
                        url.toSupabasePublicUrl(
                            supabase = supabase,
                            bucket = AVATAR_BUCKET
                        )
                    )
                }
                profile
            }
    }

    override suspend fun uploadAndSaveProfilePicture(userId: String, localUri: String): String? {
        return try {
            withContext(Dispatchers.IO) {
                val inputStream = context.contentResolver.openInputStream(localUri.toUri())
                val bytes = inputStream?.use { it.readBytes() } ?: throw Exception("Error")
                val path = "$userId.jpg"

                supabase.storage.from(AVATAR_BUCKET).upload(path, bytes, upsert = true)
                path
            }
        } catch (e: Exception) {
            Log.e(TAG, "Profile picture upload failed for uri=$localUri", e)
            null
        }
    }


    override suspend fun updateUserProfile(profile: UserProfile) {
        usersCollection.document(profile.internalID)
            .set(profile)
            .await()
    }

    override suspend fun updateCookedRecipesCount(userId: String, count: Int) {
        if (userId.isBlank()) return
        usersCollection.document(userId)
            .update("cookedRecipes", count.coerceAtLeast(0))
            .await()
    }

    override suspend fun setFollowing(currentUserId: String, targetUserId: String, follow: Boolean) {
        if (currentUserId.isBlank() || targetUserId.isBlank() || currentUserId == targetUserId) return

        val currentUserRef = usersCollection.document(currentUserId)
        val targetUserRef = usersCollection.document(targetUserId)

        firestore.runTransaction { transaction ->
            val currentUserSnapshot = transaction.get(currentUserRef)
            val followedIds = currentUserSnapshot.get("followedId") as? List<*> ?: emptyList<Any>()
            val isAlreadyFollowing = followedIds.contains(targetUserId)

            when {
                follow && !isAlreadyFollowing -> {
                    transaction.update(currentUserRef, "followedId", FieldValue.arrayUnion(targetUserId))
                    transaction.update(targetUserRef, "follower", FieldValue.increment(1))
                }
                !follow && isAlreadyFollowing -> {
                    transaction.update(currentUserRef, "followedId", FieldValue.arrayRemove(targetUserId))
                    transaction.update(targetUserRef, "follower", FieldValue.increment(-1))
                }
            }

            null
        }.await()
    }
    override suspend fun addExperience(userId: String, expToAdd: Int) {
        firestore.collection("users")
            .document(userId)
            .update("experiencePoints", FieldValue.increment(expToAdd.toLong()))
            .await()
    }
}
