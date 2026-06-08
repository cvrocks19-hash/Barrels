package com.taskflow.android.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.taskflow.android.MainActivity
import com.taskflow.android.TaskFlowApp
import com.taskflow.android.data.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@HiltWorker
class TaskNotificationWorker @AssistedInject constructor(
    @Assisted private val ctx: Context,
    @Assisted params: WorkerParameters,
    private val repository: TaskRepository,
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong(KEY_TASK_ID, -1L)
        val task   = repository.tasks().first().find { it.id == taskId } ?: return Result.success()

        val tap = PendingIntent.getActivity(
            ctx, taskId.toInt(),
            Intent(ctx, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE,
        )

        val dueLine = task.dueDate?.let {
            "Due ${it.format(DateTimeFormatter.ofPattern("EEE, MMM d 'at' h:mm a"))}"
        } ?: ""

        // Full title — never truncated (roadmap #4 kill-zone fix)
        val notification = NotificationCompat.Builder(ctx, TaskFlowApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(task.title)
            .setContentText(dueLine)
            .setStyle(NotificationCompat.BigTextStyle().bigText("${task.title}\n$dueLine"))
            .setContentIntent(tap)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        ctx.getSystemService(NotificationManager::class.java).notify(taskId.toInt(), notification)
        return Result.success()
    }

    companion object {
        const val KEY_TASK_ID = "task_id"

        fun schedule(ctx: Context, taskId: Long, dueAt: LocalDateTime) {
            val delayMinutes = Duration.between(LocalDateTime.now(), dueAt.minusMinutes(30)).toMinutes()
            if (delayMinutes < 0) return
            WorkManager.getInstance(ctx).enqueueUniqueWork(
                "task_$taskId",
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<TaskNotificationWorker>()
                    .setInputData(workDataOf(KEY_TASK_ID to taskId))
                    .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                    .addTag("task_$taskId")
                    .build(),
            )
        }

        fun cancel(ctx: Context, taskId: Long) =
            WorkManager.getInstance(ctx).cancelAllWorkByTag("task_$taskId")
    }
}
