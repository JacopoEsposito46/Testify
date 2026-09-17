package com.example.tastify.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tastify.MainActivity
import com.example.tastify.R
import com.example.tastify.models.Notification
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await

class NotificationPollingWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return Result.success()
        if (
            !NotificationWorkScheduler.areNotificationsEnabled(applicationContext, userId) ||
            !canShowNotifications()
        ) {
            return Result.success()
        }

        val firestore = FirebaseFirestore.getInstance()
        val checkStartedAt = System.currentTimeMillis()
        val lastCheckedAt = NotificationWorkScheduler.getLastCheckedTimestamp(
            applicationContext,
            userId
        )

        return try {
            val notifications = firestore.collection("users")
                .document(userId)
                .collection("notifications")
                .whereGreaterThan("timestamp", lastCheckedAt)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .limit(MAX_NOTIFICATIONS_PER_RUN)
                .get()
                .await()
                .toObjects<Notification>()

            val notificationsToDeliver = notifications.filter {
                !it.read && !it.systemNotificationDelivered
            }
            notificationsToDeliver.forEach(::showNotification)

            if (notificationsToDeliver.isNotEmpty()) {
                val batch = firestore.batch()
                notificationsToDeliver.forEach { notification ->
                    val notificationReference = firestore.collection("users")
                        .document(userId)
                        .collection("notifications")
                        .document(notification.id)
                    batch.set(
                        notificationReference,
                        mapOf("systemNotificationDelivered" to true),
                        SetOptions.merge()
                    )
                }
                batch.commit().await()
            }

            val newestTimestamp = notifications.maxOfOrNull { it.timestamp } ?: checkStartedAt
            NotificationWorkScheduler.setLastCheckedTimestamp(
                applicationContext,
                userId,
                newestTimestamp
            )
            Result.success()
        } catch (exception: Exception) {
            Result.retry()
        }
    }

    private fun canShowNotifications(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun showNotification(notification: Notification) {
        val contentIntent = Intent(applicationContext, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(
                NotificationWorkScheduler.DESTINATION_KEY,
                NotificationWorkScheduler.NOTIFICATIONS_DESTINATION
            )
        }
        val notificationId = notification.id.hashCode()
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val systemNotification = NotificationCompat.Builder(
            applicationContext,
            NotificationWorkScheduler.CHANNEL_ID
        )
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(notificationId, systemNotification)
    }

    private companion object {
        const val MAX_NOTIFICATIONS_PER_RUN = 50L
    }
}
