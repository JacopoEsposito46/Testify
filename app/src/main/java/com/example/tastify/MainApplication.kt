package com.example.tastify

import android.app.Application
import com.example.tastify.notifications.NotificationWorkScheduler
import com.example.tastify.utils.SessionManager
import dagger.hilt.android.HiltAndroidApp
import com.example.tastify.data.AuthRepository
import io.paperdb.Paper
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application() {

    @Inject
    lateinit var authRepositoryImpl: AuthRepository

    override fun onCreate() {
        super.onCreate()
        Paper.init(this)
        SessionManager.init(this, authRepositoryImpl)
        NotificationWorkScheduler.createNotificationChannel(this)
        NotificationWorkScheduler.schedule(this)
    }

}
