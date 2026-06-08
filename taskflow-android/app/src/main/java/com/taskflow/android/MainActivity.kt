package com.taskflow.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.taskflow.android.ui.screens.addtask.AddTaskSheet
import com.taskflow.android.ui.screens.tasklist.TaskListScreen
import com.taskflow.android.ui.theme.TaskFlowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()   // edge-to-edge layout (roadmap #11)
        setContent {
            TaskFlowTheme {
                var showAddSheet by remember { mutableStateOf(false) }
                TaskListScreen(onAddTask = { showAddSheet = true })
                if (showAddSheet) AddTaskSheet(onDismiss = { showAddSheet = false })
            }
        }
    }
}
