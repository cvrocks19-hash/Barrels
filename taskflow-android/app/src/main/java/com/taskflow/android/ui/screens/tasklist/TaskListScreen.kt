package com.taskflow.android.ui.screens.tasklist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.taskflow.android.ui.components.SwipeableTaskCard
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    onAddTask: () -> Unit,
    vm: TaskListViewModel = hiltViewModel(),
) {
    val state    by vm.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        vm.events.collectLatest { event ->
            when (event) {
                is UiEvent.ShowUndo -> {
                    val result = snackbar.showSnackbar(
                        message     = "“${event.task.title.take(32)}” deleted",
                        actionLabel = "Undo",
                        duration    = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) vm.undoDelete(event.task)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TaskFlow") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick  = onAddTask,
                icon     = { Text("+", style = MaterialTheme.typography.titleLarge) },
                text     = { Text("Add task") },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (state.tasks.isEmpty() && state.dailyFocus.isEmpty()) {
            EmptyState(Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(
            contentPadding = PaddingValues(
                start  = 16.dp, end = 16.dp,
                top    = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 88.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.dailyFocus.isNotEmpty()) {
                item {
                    Text(
                        "Today’s Focus",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
                items(state.dailyFocus, key = { "focus_${it.id}" }) { task ->
                    SwipeableTaskCard(task = task, onToggle = { vm.toggleStatus(task) }, onDelete = { vm.delete(task) })
                }
                item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
                item {
                    Text(
                        "All Tasks",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
            items(state.tasks, key = { it.id }) { task ->
                SwipeableTaskCard(task = task, onToggle = { vm.toggleStatus(task) }, onDelete = { vm.delete(task) })
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("✓", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(8.dp))
            Text("No tasks — tap + to add one", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
