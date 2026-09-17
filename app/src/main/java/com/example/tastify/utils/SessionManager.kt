package com.example.tastify.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.tastify.data.AuthRepository
import com.example.tastify.models.UserProfile
import com.example.tastify.notifications.NotificationWorkScheduler
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "session_prefs")

@SuppressLint("StaticFieldLeak")
object SessionManager : AuthRepository {

    private lateinit var applicationContext: Context
    private lateinit var authRepositoryImpl: AuthRepository

    fun init(context: Context, authRepo: AuthRepository) {
        applicationContext = context.applicationContext
        authRepositoryImpl = authRepo
    }

    val CURRENT_LOGGED_IN_USER_ID: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    private val isLoggedInKey = booleanPreferencesKey("is_logged_in")
    private val isDarkModeKey = booleanPreferencesKey("is_dark_mode")

    val isDarkMode: Flow<Boolean?>
        get() = applicationContext.dataStore.data.map { prefs ->
            prefs[isDarkModeKey]
        }

    suspend fun setDarkMode(enabled: Boolean) {
        applicationContext.dataStore.edit { prefs ->
            prefs[isDarkModeKey] = enabled
        }
    }

    val isLoggedIn: Flow<Boolean>
        get() = applicationContext.dataStore.data.map { prefs ->
            val dataStoreLoggedIn = prefs[isLoggedInKey] ?: false
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            dataStoreLoggedIn && firebaseUser != null
        }

    suspend fun setLoggedIn(loggedIn: Boolean) {
        applicationContext.dataStore.edit { prefs ->
            prefs[isLoggedInKey] = loggedIn
        }
    }

    override suspend fun signInWithGoogle(context: Context): Result<UserProfile> {
        return authRepositoryImpl.signInWithGoogle(context)
    }

    override suspend fun signOut(): Result<Unit> {
        NotificationWorkScheduler.cancel(applicationContext)
        val result = authRepositoryImpl.signOut()
        if (result.isSuccess) {
            setLoggedIn(false)
        }
        return result
    }
}