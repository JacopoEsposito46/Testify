package com.example.tastify.data

import android.content.Context
import com.example.tastify.models.UserProfile

interface AuthRepository {
    suspend fun signInWithGoogle(context: Context): Result<UserProfile>
    suspend fun signOut(): Result<Unit>
}