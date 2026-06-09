package com.taskflow.android.sync

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.taskflow.android.data.Priority
import com.taskflow.android.data.Recurrence
import com.taskflow.android.data.Status
import com.taskflow.android.data.Task
import com.taskflow.android.data.TaskDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SyncStatus {
    object Idle    : SyncStatus
    object Syncing : SyncStatus
    data class Error(val message: String) : SyncStatus
}

@Singleton
class TaskSyncService @Inject constructor(
    private val dao: TaskDao,
    private val auth: AuthRepository,
    private val firestore: FirebaseFirestore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val fmt   = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val status: StateFlow<SyncStatus> = _status.asStateFlow()

    private fun col(uid: String) = firestore.collection("users/$uid/tasks")

    /** Call once from Application.onCreate — starts auth-aware sync loop. */
    fun start() {
        auth.user.onEach { user ->
            if (user != null) {
                initialSync(user.uid)
                listenToRemote(user.uid)
            }
        }.launchIn(scope)
    }

    // ---- Local → Firestore ----

    suspend fun pushTask(task: Task, uid: String) {
        runCatching {
            col(uid).document(task.syncId).set(task.toMap(), SetOptions.merge()).await()
        }.onFailure { _status.value = SyncStatus.Error(it.message ?: "Push failed") }
    }

    suspend fun deleteRemoteTask(syncId: String, uid: String) {
        runCatching { col(uid).document(syncId).delete().await() }
        // best-effort — if offline, Firestore queues the delete
    }

    // ---- Firestore → Local ----

    private suspend fun initialSync(uid: String) {
        _status.value = SyncStatus.Syncing
        runCatching {
            val snap = col(uid).get().await()
            for (doc in snap.documents) {
                val remote = doc.toTask() ?: continue
                val local  = dao.getBySyncId(remote.syncId)
                when {
                    local == null                            -> dao.insert(remote)
                    remote.updatedAt.isAfter(local.updatedAt) -> dao.update(remote)
                }
            }
        }.onFailure { _status.value = SyncStatus.Error(it.message ?: "Initial sync failed") }
            .onSuccess { _status.value = SyncStatus.Idle }
    }

    private fun listenToRemote(uid: String) {
        col(uid).addSnapshotListener { snap, err ->
            if (err != null || snap == null) return@addSnapshotListener
            scope.launch {
                for (change in snap.documentChanges) {
                    when (change.type) {
                        DocumentChange.Type.ADDED,
                        DocumentChange.Type.MODIFIED -> {
                            val remote = change.document.toTask() ?: continue
                            val local  = dao.getBySyncId(remote.syncId)
                            when {
                                local == null                               -> dao.insert(remote)
                                remote.updatedAt.isAfter(local.updatedAt)  -> dao.update(remote)
                                // local is newer or same — keep local
                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            dao.getBySyncId(change.document.id)?.let { dao.delete(it) }
                        }
                    }
                }
            }
        }
    }

    // ---- Serialisation ----

    private fun Task.toMap(): Map<String, Any?> = mapOf(
        "syncId"      to syncId,
        "title"       to title,
        "description" to description,
        "dueDate"     to dueDate?.format(fmt),
        "priority"    to priority.name,
        "status"      to status.name,
        "isHabit"     to isHabit,
        "recurrence"  to recurrence?.name,
        "createdAt"   to createdAt.format(fmt),
        "updatedAt"   to updatedAt.format(fmt),
        "completedAt" to completedAt?.format(fmt),
    )

    private fun DocumentSnapshot.toTask(): Task? = runCatching {
        Task(
            syncId      = getString("syncId") ?: id,
            title       = getString("title")  ?: return@runCatching null,
            description = getString("description") ?: "",
            dueDate     = getString("dueDate")?.let     { LocalDateTime.parse(it, fmt) },
            priority    = getString("priority")?.let    { Priority.valueOf(it) }    ?: Priority.MEDIUM,
            status      = getString("status")?.let      { Status.valueOf(it) }      ?: Status.TODO,
            isHabit     = getBoolean("isHabit") ?: false,
            recurrence  = getString("recurrence")?.let  { Recurrence.valueOf(it) },
            createdAt   = getString("createdAt")?.let   { LocalDateTime.parse(it, fmt) } ?: LocalDateTime.now(),
            updatedAt   = getString("updatedAt")?.let   { LocalDateTime.parse(it, fmt) } ?: LocalDateTime.now(),
            completedAt = getString("completedAt")?.let { LocalDateTime.parse(it, fmt) },
        )
    }.getOrNull()
}
