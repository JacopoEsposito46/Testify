package com.example.tastify.data

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.tastify.R
import com.example.tastify.models.ImageSource
import com.example.tastify.models.UserProfile
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {
    override suspend fun signInWithGoogle(context: Context): Result<UserProfile> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setServerClientId(context.getString(R.string.default_web_client_id))
                .setFilterByAuthorizedAccounts(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(context)
            val result = credentialManager.getCredential(context, request)
            val credential = result.credential

            if (credential !is CustomCredential || credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                return Result.failure(Exception("Credential not valid"))
            }
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val googleIdToken = googleIdTokenCredential.idToken
            val firebaseAuthCredential = GoogleAuthProvider.getCredential(googleIdToken, null)

            val authResult = firebaseAuth.signInWithCredential(firebaseAuthCredential).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("User null"))

            val fullName = firebaseUser.displayName ?: ""
            val nameParts = fullName.split(" ")
            val name = nameParts.firstOrNull() ?: ""
            val surname = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else ""
//            val username = firebaseUser.email?.substringBefore("@")

            val docRef = firestore.collection("users").document(firebaseUser.uid)
            val existingDoc = docRef.get().await()

            val userProfile = if (existingDoc.exists()) {
                existingDoc.toObject<UserProfile>() ?: UserProfile(internalID = firebaseUser.uid)
            } else {
                UserProfile(
                    internalID = firebaseUser.uid,
                    name = name,
                    surname = surname,
                    email = firebaseUser.email ?: "",
                    username = "",
                    profilePictureUrl = firebaseUser.photoUrl?.toString(),
                    profilePicture = ImageSource.Uri(firebaseUser.photoUrl?.toString() ?: "")
                )
            }

            Result.success(userProfile)
        } catch (e: GetCredentialException) {
            Log.e("AuthRepository", "Google error", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Generic error", e)
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}