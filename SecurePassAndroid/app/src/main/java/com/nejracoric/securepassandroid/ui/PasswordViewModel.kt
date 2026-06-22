package com.nejracoric.securepassandroid.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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

data class PasswordUiState(
    val password: String = "",
    val length: Int = 16,
    val includeSpecialChars: Boolean = true,
    val entropyBits: Double = 0.0,
    val savedPasswords: List<String> = emptyList(),
    val averageSessionEntropy: Double = 0.0,
    val pwnedCount: Int = 0,
    val isCheckingPwned: Boolean = false,
    val pwnedCheckFailed: Boolean = false,
    val pwnedCheckFailedMessage: String? = null,
    val errorMessage: String? = null,
)

class PasswordViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PasswordUiState())
    val uiState: StateFlow<PasswordUiState> = _uiState.asStateFlow()

    private var pwnedCheckJob: Job? = null

    companion object {
        private const val TAG = "PasswordViewModel"
    }

    fun setLength(length: Int) {
        _uiState.update { it.copy(length = length.coerceIn(8, 32)) }
    }

    fun setIncludeSpecialChars(enabled: Boolean) {
        _uiState.update { it.copy(includeSpecialChars = enabled) }
    }

    fun setPassword(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                entropyBits = SecurePassNative.calculateEntropy(password),
                errorMessage = null,
            )
        }
        checkPwnedStatus(password)
    }

    fun generatePassword() {
        val state = _uiState.value
        SecurePassNative.generatePassword(state.length, state.includeSpecialChars)
            .onSuccess { password ->
                setPassword(password)
            }
            .onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message) }
            }
    }

    fun retryPwnedCheck() {
        checkPwnedStatus(_uiState.value.password)
    }

    fun savePasswordToList() {
        val password = _uiState.value.password
        if (password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Unesi lozinku prije čuvanja") }
            return
        }

        val updatedList = _uiState.value.savedPasswords + password
        _uiState.update {
            it.copy(
                savedPasswords = updatedList,
                averageSessionEntropy = calculateAverageEntropy(updatedList),
                errorMessage = null,
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
                it.copy(
                    isCheckingPwned = true,
                    pwnedCheckFailed = false,
                    pwnedCheckFailedMessage = null,
                )
            }

            try {
                delay(400)
                ensureActive()

                if (_uiState.value.password != password) return@launch

                if (!NetworkUtils.isOnline(getApplication())) {
                    failPwnedCheck(
                        password = password,
                        message = "Nema internetske veze. Uključi Wi-Fi ili mobilne podatke.",
                    )
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
                val message = withContext(Dispatchers.IO) {
                    mapPwnedError(error)
                }
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
        Log.e(TAG, "HIBP check failed for prefix lookup", error)

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
            is UnknownHostException ->
                "HIBP server nije dostupan. Provjeri DNS ili mrežu."

            is SocketTimeoutException ->
                "Isteklo je vrijeme čekanja na HIBP API. Isključi VPN/proxy ili probaj drugu mrežu."

            is IOException ->
                rootCause.message ?: "Provjera protiv HIBP baze trenutno nije dostupna."

            else ->
                "Provjera protiv HIBP baze trenutno nije dostupna."
        }
    }

    private fun calculateAverageEntropy(passwords: List<String>): Double {
        if (passwords.isEmpty()) return 0.0
        return passwords
            .map { SecurePassNative.calculateEntropy(it) }
            .average()
    }
}
