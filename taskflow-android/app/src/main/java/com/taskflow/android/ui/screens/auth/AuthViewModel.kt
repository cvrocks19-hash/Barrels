package com.taskflow.android.ui.screens.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.taskflow.android.sync.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    val user: StateFlow<FirebaseUser?> = authRepository.user

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun getSignInIntent(): Intent = authRepository.googleSignInClient.signInIntent

    fun signInWithGoogle(idToken: String) = viewModelScope.launch {
        _isLoading.value = true
        _error.value = null
        runCatching { authRepository.signInWithGoogle(idToken) }
            .onFailure { _error.value = "Sign-in failed: ${it.message}" }
        _isLoading.value = false
    }

    fun setError(msg: String)  { _error.value = msg }
    fun signOut()              = authRepository.signOut()
}
