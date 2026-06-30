package com.nejracoric.securepassandroid.ui.generator

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nejracoric.securepassandroid.data.repository.VaultRepository
import com.nejracoric.securepassandroid.native.SecurePassNative
import com.nejracoric.securepassandroid.security.NetworkUtils
import com.nejracoric.securepassandroid.security.PwnedNetworkDiagnostics
import com.nejracoric.securepassandroid.security.PwnedNetworkIssue
import com.nejracoric.securepassandroid.security.PwnedPasswordChecker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

data class GeneratorUiState(
    val password: String = "",
    val length: Int = 20,
    val includeUppercase: Boolean = true,
    val includeLowercase: Boolean = true,
    val includeNumbers: Boolean = true,
    val includeSpecialChars: Boolean = true,
    val entropyBits: Double = 0.0,
    val pwnedCount: Int = 0,
    val isCheckingPwned: Boolean = false,
    val pwnedCheckFailed: Boolean = false,
    val pwnedCheckFailedMessage: String? = null,
    val errorMessage: String? = null,
    val showSaveSheet: Boolean = false,
    val saveTitle: String = "",
    val saveUsername: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
) {
    val securityProgress: Float
        get() = (entropyBits / 100.0).coerceIn(0.0, 1.0).toFloat()
}

class GeneratorViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val vaultRepository = VaultRepository()
    private val _uiState = MutableStateFlow(GeneratorUiState())
    val uiState: StateFlow<GeneratorUiState> = _uiState.asStateFlow()

    private var pwnedCheckJob: Job? = null

    companion object {
        private const val TAG = "GeneratorViewModel"
    }

    fun setLength(length: Int) {
        _uiState.update { it.copy(length = length.coerceIn(8, 32)) }
    }

    fun setIncludeSpecialChars(enabled: Boolean) {
        _uiState.update { it.copy(includeSpecialChars = enabled) }
    }

    fun setIncludeUppercase(enabled: Boolean) {
        _uiState.update { it.copy(includeUppercase = enabled) }
    }

    fun setIncludeLowercase(enabled: Boolean) {
        _uiState.update { it.copy(includeLowercase = enabled) }
    }

    fun setIncludeNumbers(enabled: Boolean) {
        _uiState.update { it.copy(includeNumbers = enabled) }
    }

    fun setPassword(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                entropyBits = SecurePassNative.calculateEntropy(password),
                errorMessage = null,
                saveSuccess = false,
            )
        }
        checkPwnedStatus(password)
    }

    fun generatePassword() {
        val state = _uiState.value
        SecurePassNative.generatePassword(state.length, state.includeSpecialChars)
            .onSuccess { password -> setPassword(password) }
            .onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message) }
            }
    }

    fun retryPwnedCheck() {
        checkPwnedStatus(_uiState.value.password)
    }

    fun showSaveSheet() {
        if (_uiState.value.password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Generiši lozinku prije čuvanja") }
            return
        }
        _uiState.update { it.copy(showSaveSheet = true, saveSuccess = false) }
    }

    fun dismissSaveSheet() {
        _uiState.update {
            it.copy(showSaveSheet = false, saveTitle = "", saveUsername = "")
        }
    }

    fun setSaveTitle(title: String) {
        _uiState.update { it.copy(saveTitle = title) }
    }

    fun setSaveUsername(username: String) {
        _uiState.update { it.copy(saveUsername = username) }
    }

    fun saveToVault(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.saveTitle.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Unesi naziv platforme") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            vaultRepository.createPassword(
                title = state.saveTitle,
                username = state.saveUsername.ifBlank { null },
                password = state.password,
            ).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            showSaveSheet = false,
                            saveTitle = "",
                            saveUsername = "",
                            saveSuccess = true,
                        )
                    }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isSaving = false, errorMessage = error.message)
                    }
                },
            )
        }
    }

    private fun checkPwnedStatus(password: String) {
        pwnedCheckJob?.cancel()

        if (password.isEmpty()) {
            _uiState.update {
                it.copy(
                    pwnedCount = 0,
                    isCheckingPwned = false,
                    pwnedCheckFailed = false,
                    pwnedCheckFailedMessage = null,
                )
            }
            return
        }

        pwnedCheckJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isCheckingPwned = true, pwnedCheckFailed = false, pwnedCheckFailedMessage = null)
            }

            try {
                delay(400)
                ensureActive()
                if (_uiState.value.password != password) return@launch

                if (!NetworkUtils.isOnline(getApplication())) {
                    failPwnedCheck(password, "Nema internetske veze. Uključi Wi-Fi ili mobilne podatke.")
                    return@launch
                }

                val count = withContext(Dispatchers.IO) {
                    PwnedPasswordChecker.checkPassword(password)
                }

                if (_uiState.value.password != password) return@launch

                _uiState.update {
                    it.copy(
                        pwnedCount = count,
                        isCheckingPwned = false,
                        pwnedCheckFailed = false,
                        pwnedCheckFailedMessage = null,
                    )
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val message = withContext(Dispatchers.IO) { mapPwnedError(error) }
                failPwnedCheck(password, message)
            }
        }
    }

    private fun failPwnedCheck(password: String, message: String) {
        if (_uiState.value.password != password) return
        Log.e(TAG, "HIBP check failed: $message")
        _uiState.update {
            it.copy(
                pwnedCount = 0,
                isCheckingPwned = false,
                pwnedCheckFailed = true,
                pwnedCheckFailedMessage = message,
            )
        }
    }

    private fun mapPwnedError(error: Exception): String {
        val networkIssue = PwnedNetworkDiagnostics.diagnose()
        when (networkIssue) {
            PwnedNetworkIssue.NoInternet ->
                return "Nema internetske veze. Uključi Wi-Fi ili mobilne podatke."
            PwnedNetworkIssue.HibpBlocked ->
                return "Mreža blokira pristup HIBP API-ju. Probaj mobilne podatke ili drugi Wi-Fi."
            PwnedNetworkIssue.Ok -> Unit
        }

        val rootCause = generateSequence<Throwable>(error) { it.cause }.last()
        return when (rootCause) {
            is UnknownHostException -> "HIBP server nije dostupan. Provjeri DNS ili mrežu."
            is SocketTimeoutException -> "Isteklo je vrijeme čekanja na HIBP API."
            is IOException -> rootCause.message ?: "Provjera protiv HIBP baze nije dostupna."
            else -> "Provjera protiv HIBP baze nije dostupna."
        }
    }
}
