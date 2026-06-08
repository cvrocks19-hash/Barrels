package com.taskflow.android.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDate: LocalDateTime? = null,
    val priority: Priority = Priority.MEDIUM,
    val status: Status = Status.TODO,
    val isHabit: Boolean = false,
    val recurrence: Recurrence? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null,
)

enum class Priority { LOW, MEDIUM, HIGH }
enum class Status   { TODO, IN_PROGRESS, DONE }
enum class Recurrence { DAILY, WEEKLY, BIWEEKLY, MONTHLY, CUSTOM }
