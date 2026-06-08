package com.taskflow.android.ui.screens.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.android.data.Task
import com.taskflow.android.data.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val dailyFocus: List<Task> = emptyList(),
    val pendingCount: Int = 0,
)

sealed interface UiEvent {
    data class ShowUndo(val task: Task) : UiEvent
}

@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val repository: TaskRepository,
) : ViewModel() {

    val uiState: StateFlow<TaskListUiState> = combine(
        repository.tasks(),
        repository.dailyFocus(),
        repository.countPending(),
    ) { tasks, focus, pending ->
        TaskListUiState(tasks = tasks, dailyFocus = focus, pendingCount = pending)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskListUiState())

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun toggleStatus(task: Task) = viewModelScope.launch { repository.cycleStatus(task) }

    fun delete(task: Task) = viewModelScope.launch {
        repository.delete(task)
        _events.send(UiEvent.ShowUndo(task))
    }

    fun undoDelete(task: Task) = viewModelScope.launch { repository.add(task) }
}
