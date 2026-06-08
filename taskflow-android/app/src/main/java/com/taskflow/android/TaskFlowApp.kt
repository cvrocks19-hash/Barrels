package com.taskflow.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TaskFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Task Reminders", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Reminders 30 min before tasks are due" }
        )
    }
    companion object { const val CHANNEL_ID = "task_reminders" }
}
