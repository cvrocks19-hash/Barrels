package com.taskflow.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.taskflow.android.ui.screens.addtask.AddTaskSheet
import com.taskflow.android.ui.screens.auth.AuthViewModel
import com.taskflow.android.ui.screens.auth.SignInScreen
import com.taskflow.android.ui.screens.tasklist.TaskListScreen
import com.taskflow.android.ui.theme.TaskFlowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskFlowTheme {
                val authVm: AuthViewModel = hiltViewModel()
                val user by authVm.user.collectAsState()
                var showAddSheet by remember { mutableStateOf(false) }

                if (user == null) {
                    // Not signed in — show sign-in (counts as roadmap #10 onboarding entry)
                    SignInScreen(vm = authVm)
                } else {
                    TaskListScreen(onAddTask = { showAddSheet = true })
                    if (showAddSheet) AddTaskSheet(onDismiss = { showAddSheet = false })
                }
            }
        }
    }
}
