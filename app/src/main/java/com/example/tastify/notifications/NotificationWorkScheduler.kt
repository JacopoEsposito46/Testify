package com.example.tastify.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

object NotificationWorkScheduler {

    const val CHANNEL_ID = "tastify_notifications"
    const val DESTINATION_KEY = "destination"
    const val NOTIFICATIONS_DESTINATION = "notifications"

    private const val WORK_NAME = "notification_polling"
    private const val PREFERENCES_NAME = "notification_worker_prefs"
    private const val LAST_CHECKED_PREFIX = "last_checked_"
    private const val ENABLED_PREFIX = "enabled_"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Tastify notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Recipe updates, reviews and recommendations"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun schedule(context: Context) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (!areNotificationsEnabled(context, userId)) return
        initializeLastCheckedTimestamp(context, userId)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<NotificationPollingWorker>(
            15,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    fun getLastCheckedTimestamp(context: Context, userId: String): Long {
        return preferences(context).getLong(lastCheckedKey(userId), System.currentTimeMillis())
    }

    fun setLastCheckedTimestamp(context: Context, userId: String, timestamp: Long) {
        preferences(context)
            .edit()
            .putLong(lastCheckedKey(userId), timestamp)
            .apply()
    }

    fun setNotificationsEnabled(context: Context, userId: String, enabled: Boolean) {
        preferences(context)
            .edit()
            .putBoolean(enabledKey(userId), enabled)
            .apply()
    }

    fun areNotificationsEnabled(context: Context, userId: String): Boolean {
        return preferences(context).getBoolean(enabledKey(userId), true)
    }

    fun markNotificationsViewed(context: Context, userId: String, timestamp: Long) {
        val currentTimestamp = getLastCheckedTimestamp(context, userId)
        if (timestamp > currentTimestamp) {
            setLastCheckedTimestamp(context, userId, timestamp)
        }
    }

    private fun initializeLastCheckedTimestamp(context: Context, userId: String) {
        val preferences = preferences(context)
        val key = lastCheckedKey(userId)
        if (!preferences.contains(key)) {
            preferences.edit().putLong(key, System.currentTimeMillis()).apply()
        }
    }

    private fun preferences(context: Context) =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private fun lastCheckedKey(userId: String) = "$LAST_CHECKED_PREFIX$userId"

    private fun enabledKey(userId: String) = "$ENABLED_PREFIX$userId"
}
