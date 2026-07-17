package com.example.prima.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prima.data.SessionManager
import com.example.prima.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val userRole: String? = null,
    val userName: String? = null
)

sealed class AuthEvent {
    data object LoginSuccess : AuthEvent()
    data object LogoutSuccess : AuthEvent()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()
    val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    init {
        checkSession()
    }

    private fun checkSession() {
        if (sessionManager.isLoggedIn()) {
            _uiState.value = AuthUiState(
                isLoggedIn = true,
                userRole = sessionManager.getUserRole(),
                userName = sessionManager.getUserName()
            )
        }
    }

    fun login(email: String, password: String) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = repository.login(email, password)
            result.onSuccess { response ->
                val token = response.token
                val user = response.user

                if (token != null && user != null) {
                    sessionManager.saveSession(
                        token = token,
                        userId = user.id,
                        name = user.name,
                        email = user.email,
                        role = user.role
                    )
                    _uiState.value = AuthUiState(
                        isLoggedIn = true,
                        userRole = user.role,
                        userName = user.name
                    )
                    _events.emit(AuthEvent.LoginSuccess)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Respons login tidak valid dari server"
                    )
                }
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Login gagal"
                )
            }
        }
    }

    fun logout() {
        sessionManager.clearSession()
        _uiState.value = AuthUiState()

        viewModelScope.launch {
            val token = sessionManager.getToken()
            if (token != null) {
                repository.logout(token)
            }
            _events.emit(AuthEvent.LogoutSuccess)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
