package com.taskflow.android.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val dao: TaskDao,
    private val sync: com.taskflow.android.sync.TaskSyncService,
    private val auth: com.taskflow.android.sync.AuthRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val fmt   = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    fun tasks(): Flow<List<Task>>      = dao.observeTasks()
    fun habits(): Flow<List<Task>>     = dao.observeHabits()
    fun dailyFocus(): Flow<List<Task>> = dao.observeDailyFocus()
    fun countPending(): Flow<Int>      = dao.countPending()

    suspend fun add(task: Task): Long {
        val id = dao.insert(task)
        pushAsync(task.copy(id = id))
        return id
    }

    suspend fun update(task: Task) {
        val updated = task.copy(updatedAt = LocalDateTime.now())
        dao.update(updated)
        pushAsync(updated)
    }

    suspend fun delete(task: Task) {
        dao.delete(task)
        auth.user.value?.uid?.let { uid ->
            scope.launch { sync.deleteRemoteTask(task.syncId, uid) }
        }
    }

    suspend fun cycleStatus(task: Task) {
        val now = LocalDateTime.now()
        val (nextStatus, completedAt) = when (task.status) {
            Status.TODO        -> Status.IN_PROGRESS to null
            Status.IN_PROGRESS -> Status.DONE        to now
            Status.DONE        -> Status.TODO        to null
        }
        dao.updateStatus(task.id, nextStatus.name, completedAt?.format(fmt), now.format(fmt))
        pushAsync(task.copy(status = nextStatus, updatedAt = now, completedAt = completedAt))
    }

    private fun pushAsync(task: Task) {
        val uid = auth.user.value?.uid ?: return
        scope.launch { sync.pushTask(task, uid) }
    }
}
