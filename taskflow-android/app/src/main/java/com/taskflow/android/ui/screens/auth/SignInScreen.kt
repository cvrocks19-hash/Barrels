package com.taskflow.android.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

@Composable
fun SignInScreen(vm: AuthViewModel) {
    val isLoading by vm.isLoading.collectAsState()
    val error     by vm.error.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val account = GoogleSignIn
                    .getSignedInAccountFromIntent(result.data)
                    .getResult(ApiException::class.java)
                account.idToken?.let { vm.signInWithGoogle(it) }
                    ?: vm.setError("No ID token — check SHA-1 in Firebase console.")
            } catch (e: ApiException) {
                vm.setError("Google sign-in failed (code ${e.statusCode})")
            }
        }
    }

    Box(
        Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "TaskFlow",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "Your tasks. Every device.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick   = { launcher.launch(vm.getSignInIntent()) },
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = MaterialTheme.shapes.medium,
                ) {
                    Text("Continue with Google")
                }
            }

            error?.let {
                Text(
                    it,
                    color  = MaterialTheme.colorScheme.error,
                    style  = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
