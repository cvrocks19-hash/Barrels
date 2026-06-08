package com.taskflow.android.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(private val dao: TaskDao) {

    fun tasks(): Flow<List<Task>>       = dao.observeTasks()
    fun habits(): Flow<List<Task>>      = dao.observeHabits()
    fun dailyFocus(): Flow<List<Task>>  = dao.observeDailyFocus()
    fun countPending(): Flow<Int>       = dao.countPending()

    suspend fun add(task: Task): Long   = dao.insert(task)
    suspend fun update(task: Task)      = dao.update(task)
    suspend fun delete(task: Task)      = dao.delete(task)

    suspend fun cycleStatus(task: Task) {
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val (next, completedAt) = when (task.status) {
            Status.TODO        -> Status.IN_PROGRESS to null
            Status.IN_PROGRESS -> Status.DONE        to LocalDateTime.now().format(fmt)
            Status.DONE        -> Status.TODO        to null
        }
        dao.updateStatus(task.id, next.name, completedAt)
    }
}
