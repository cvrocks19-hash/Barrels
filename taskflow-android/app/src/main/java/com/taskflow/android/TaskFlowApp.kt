package com.taskflow.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.taskflow.android.sync.TaskSyncService
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TaskFlowApp : Application() {

    @Inject lateinit var syncService: TaskSyncService

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        syncService.start()   // begins auth-aware Firestore sync loop
    }

    private fun createNotificationChannel() {
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Task Reminders", NotificationManager.IMPORTANCE_HIGH)
                .apply { description = "Reminders 30 min before tasks are due" }
        )
    }

    companion object { const val CHANNEL_ID = "task_reminders" }
}
