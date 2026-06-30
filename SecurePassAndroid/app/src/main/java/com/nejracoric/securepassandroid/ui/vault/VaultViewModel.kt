package com.nejracoric.securepassandroid.ui.vault

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nejracoric.securepassandroid.data.api.PasswordEntryDto
import com.nejracoric.securepassandroid.data.repository.VaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VaultUiState(
    val passwords: List<PasswordEntryDto> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedEntry: PasswordEntryDto? = null,
    val isPasswordRevealed: Boolean = false,
) {
    val filteredPasswords: List<PasswordEntryDto>
        get() {
            if (searchQuery.isBlank()) return passwords
            val query = searchQuery.lowercase()
            return passwords.filter { entry ->
                entry.title.lowercase().contains(query) ||
                    entry.username?.lowercase()?.contains(query) == true
            }
        }
}

class VaultViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val repository = VaultRepository()

    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    fun loadPasswords() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getPasswords().fold(
                onSuccess = { list ->
                    _uiState.update {
                        it.copy(passwords = list, isLoading = false)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = error.message)
                    }
                },
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectEntry(entry: PasswordEntryDto) {
        _uiState.update {
            it.copy(selectedEntry = entry, isPasswordRevealed = false)
        }
    }

    fun dismissDetail() {
        _uiState.update {
            it.copy(selectedEntry = null, isPasswordRevealed = false)
        }
    }

    fun revealPassword() {
        _uiState.update { it.copy(isPasswordRevealed = true) }
    }

    fun hidePassword() {
        _uiState.update { it.copy(isPasswordRevealed = false) }
    }

    fun deletePassword(id: String) {
        viewModelScope.launch {
            repository.deletePassword(id).fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(
                            passwords = state.passwords.filter { it.id != id },
                            selectedEntry = null,
                            isPasswordRevealed = false,
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                },
            )
        }
    }
}
