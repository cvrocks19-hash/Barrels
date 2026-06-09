package com.taskflow.android.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface TaskDao {

    @Query("""
        SELECT * FROM tasks WHERE isHabit = 0
        ORDER BY
            CASE status WHEN 'TODO' THEN 0 WHEN 'IN_PROGRESS' THEN 1 ELSE 2 END,
            CASE priority WHEN 'HIGH' THEN 0 WHEN 'MEDIUM' THEN 1 ELSE 2 END,
            dueDate ASC
    """)
    fun observeTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isHabit = 1 ORDER BY title ASC")
    fun observeHabits(): Flow<List<Task>>

    @Query("""
        SELECT * FROM tasks
        WHERE isHabit = 0 AND status != 'DONE'
        ORDER BY
            CASE WHEN dueDate IS NOT NULL AND dueDate <= :tomorrow THEN 0 ELSE 1 END,
            CASE priority WHEN 'HIGH' THEN 0 WHEN 'MEDIUM' THEN 1 ELSE 2 END
        LIMIT 5
    """)
    fun observeDailyFocus(
        tomorrow: String = LocalDateTime.now().plusDays(1)
            .format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    ): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE syncId = :syncId LIMIT 1")
    suspend fun getBySyncId(syncId: String): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("""
        UPDATE tasks
        SET status = :status, completedAt = :completedAt, updatedAt = :updatedAt
        WHERE id = :id
    """)
    suspend fun updateStatus(id: Long, status: String, completedAt: String?, updatedAt: String)

    @Query("SELECT COUNT(*) FROM tasks WHERE status != 'DONE' AND isHabit = 0")
    fun countPending(): Flow<Int>
}
