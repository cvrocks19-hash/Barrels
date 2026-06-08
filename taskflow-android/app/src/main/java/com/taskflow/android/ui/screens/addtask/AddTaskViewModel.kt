package com.taskflow.android.ui.screens.addtask

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskflow.android.data.Task
import com.taskflow.android.data.TaskRepository
import com.taskflow.android.domain.ParsedTask
import com.taskflow.android.domain.ParseTaskTextUseCase
import com.taskflow.android.notifications.TaskNotificationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTaskViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val parser: ParseTaskTextUseCase,
    @ApplicationContext private val ctx: Context,
) : ViewModel() {

    private val _text    = MutableStateFlow("")
    private val _preview = MutableStateFlow<ParsedTask?>(null)
    val text    = _text.asStateFlow()
    val preview = _preview.asStateFlow()

    fun onTextChange(value: String) {
        _text.value    = value
        _preview.value = if (value.isNotBlank()) parser.parse(value) else null
    }

    fun submit(onDone: () -> Unit) = viewModelScope.launch {
        val parsed = _preview.value ?: return@launch
        val id = repository.add(
            Task(title = parsed.title, dueDate = parsed.dueDate, priority = parsed.priority)
        )
        parsed.dueDate?.let { TaskNotificationWorker.schedule(ctx, id, it) }
        _text.value    = ""
        _preview.value = null
        onDone()
    }
}
