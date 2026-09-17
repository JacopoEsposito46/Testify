package com.example.tastify.data

import com.example.tastify.models.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseNotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    private fun getNotificationsCollection(userId: String) =
        firestore.collection("users").document(userId).collection("notifications")

    private fun getUserDocument(userId: String) =
        firestore.collection("users").document(userId)

    override fun getNotificationsForUser(userId: String): Flow<List<Notification>> {
        return getNotificationsCollection(userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects<Notification>() }
    }

    override fun getUnreadCount(userId: String): Flow<Int> {
        return getNotificationsCollection(userId)
            .whereEqualTo("read", false)
            .snapshots()
            .map { snapshot -> snapshot.size() }
    }

    override fun observeNotificationsEnabled(userId: String): Flow<Boolean> {
        return getUserDocument(userId)
            .snapshots()
            .map { snapshot -> snapshot.getBoolean("notificationsEnabled") ?: true }
    }

    override suspend fun setNotificationsEnabled(userId: String, enabled: Boolean) {
        getUserDocument(userId)
            .set(mapOf("notificationsEnabled" to enabled), SetOptions.merge())
            .await()
    }

    override suspend fun markAsRead(userId: String, notificationId: String) {
        getNotificationsCollection(userId)
            .document(notificationId)
            .update("read", true)
            .await()
    }

    override suspend fun markAllAsRead(userId: String) {
        val unreadSnapshot = getNotificationsCollection(userId)
            .whereEqualTo("read", false)
            .get()
            .await()

        if (unreadSnapshot.isEmpty) return

        val batch = firestore.batch()
        for (document in unreadSnapshot.documents) {
            batch.update(document.reference, "read", true)
        }
        batch.commit().await()
    }

    override suspend fun createNotification(notification: Notification) {
        val notificationsEnabled = getUserDocument(notification.recipientId)
            .get()
            .await()
            .getBoolean("notificationsEnabled") ?: true

        if (!notificationsEnabled) return

        val targetId = notification.id.ifBlank { getNotificationsCollection(notification.recipientId).document().id }
        val finalNotification = notification.copy(id = targetId)

        getNotificationsCollection(notification.recipientId)
            .document(targetId)
            .set(finalNotification)
            .await()
    }

    override suspend fun deleteNotification(userId: String, notificationId: String) {
        getNotificationsCollection(userId)
            .document(notificationId)
            .delete()
            .await()
    }
}
