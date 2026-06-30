package com.nejracoric.securepassandroid.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nejracoric.securepassandroid.data.local.TokenManager
import com.nejracoric.securepassandroid.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isRegisterMode: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasStoredSession: Boolean = false,
    val storedEmail: String? = null,
)

class AuthViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val authRepository = AuthRepository(tokenManager)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val hasSession = authRepository.hasSession()
            val email = authRepository.getStoredEmail()
            _uiState.update {
                it.copy(
                    hasStoredSession = hasSession,
                    storedEmail = email,
                    email = email ?: "",
                )
            }
        }
    }

    fun setEmail(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun setPassword(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun toggleMode() {
        _uiState.update {
            it.copy(
                isRegisterMode = !it.isRegisterMode,
                errorMessage = null,
            )
        }
    }

    fun submit(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email i lozinka su obavezni") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = if (state.isRegisterMode) {
                authRepository.register(state.email, state.password)
            } else {
                authRepository.login(state.email, state.password)
            }

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message,
                        )
                    }
                },
            )
        }
    }

    fun biometricLogin(onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (authRepository.hasSession()) {
                tokenManager.getToken()
                onSuccess()
            } else {
                _uiState.update {
                    it.copy(errorMessage = "Nema sačuvane sesije. Prijavi se emailom i lozinkom.")
                }
            }
        }
    }
}
