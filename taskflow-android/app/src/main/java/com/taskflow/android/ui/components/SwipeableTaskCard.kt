package com.taskflow.android.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.taskflow.android.data.Priority
import com.taskflow.android.data.Status
import com.taskflow.android.data.Task
import com.taskflow.android.ui.theme.PriorityHigh
import com.taskflow.android.ui.theme.PriorityLow
import com.taskflow.android.ui.theme.PriorityMedium
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTaskCard(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> { onToggle(); false }   // swipe right = complete
                SwipeToDismissBoxValue.EndToStart -> { onDelete(); true  }   // swipe left  = delete
                else -> false
            }
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = { SwipeBackground(dismissState) },
    ) {
        TaskCard(task = task, onClick = onToggle)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBackground(state: SwipeToDismissBoxState) {
    val scale by animateFloatAsState(
        if (state.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f
    )
    val (color, label, gravity) = when (state.dismissDirection) {
        SwipeToDismissBoxValue.StartToEnd -> Triple(Color(0xFF22C55E), "✓", Alignment.CenterStart)
        SwipeToDismissBoxValue.EndToStart -> Triple(Color(0xFFEF4444), "🗑", Alignment.CenterEnd)
        else                              -> Triple(Color.Transparent, "",   Alignment.Center)
    }
    Box(
        Modifier.fillMaxSize().background(color).padding(horizontal = 20.dp),
        contentAlignment = gravity,
    ) {
        Text(label, modifier = Modifier.scale(scale), style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun TaskCard(task: Task, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val priorityColor = when (task.priority) {
        Priority.HIGH   -> PriorityHigh
        Priority.MEDIUM -> PriorityMedium
        Priority.LOW    -> PriorityLow
    }
    val done = task.status == Status.DONE

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (done) MaterialTheme.colorScheme.surfaceVariant
                             else      MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (done) 0.dp else 2.dp),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .width(4.dp).height(40.dp)
                    .background(
                        color = if (done) Color.Transparent else priorityColor,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    textDecoration = if (done) TextDecoration.LineThrough else null,
                    color = if (done) MaterialTheme.colorScheme.onSurfaceVariant
                            else     MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                )
                if (task.dueDate != null) {
                    Text(
                        task.dueDate.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (task.description.isNotEmpty()) {
                    Text(
                        task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            Text(
                text = when (task.status) {
                    Status.TODO        -> "○"
                    Status.IN_PROGRESS -> "◑"
                    Status.DONE        -> "●"
                },
                style = MaterialTheme.typography.titleMedium,
                color = if (done) MaterialTheme.colorScheme.primary
                        else     MaterialTheme.colorScheme.outline,
            )
        }
    }
}
