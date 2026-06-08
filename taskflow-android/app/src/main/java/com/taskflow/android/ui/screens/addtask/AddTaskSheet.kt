package com.taskflow.android.ui.screens.addtask

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.taskflow.android.data.Priority
import com.taskflow.android.domain.ParsedTask
import com.taskflow.android.ui.theme.PriorityHigh
import com.taskflow.android.ui.theme.PriorityLow
import com.taskflow.android.ui.theme.PriorityMedium
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskSheet(
    onDismiss: () -> Unit,
    vm: AddTaskViewModel = hiltViewModel(),
) {
    val text    by vm.text.collectAsState()
    val preview by vm.preview.collectAsState()
    val focus   = remember { FocusRequester() }

    LaunchedEffect(Unit) { focus.requestFocus() }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
        ) {
            Text("New task", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value         = text,
                onValueChange = vm::onTextChange,
                modifier      = Modifier.fillMaxWidth().focusRequester(focus),
                placeholder   = { Text("“Call dentist tomorrow at 3pm”") },
                singleLine    = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (text.isNotBlank()) vm.submit(onDismiss)
                }),
            )

            preview?.let { NlpPreviewCard(it) }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick  = { vm.submit(onDismiss) },
                enabled  = text.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Add Task") }
        }
    }
}

@Composable
private fun NlpPreviewCard(preview: ParsedTask) {
    Spacer(Modifier.height(12.dp))
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(preview.title, style = MaterialTheme.typography.bodyMedium)
                preview.dueDate?.let {
                    Text(
                        it.format(DateTimeFormatter.ofPattern("EEE MMM d 'at' h:mm a")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            val (label, color) = when (preview.priority) {
                Priority.HIGH   -> "High"   to PriorityHigh
                Priority.MEDIUM -> "Medium" to PriorityMedium
                Priority.LOW    -> "Low"    to PriorityLow
            }
            Surface(
                color = color.copy(alpha = 0.2f),
                shape = MaterialTheme.shapes.extraSmall,
            ) {
                Text(
                    label,
                    Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                )
            }
        }
    }
}
