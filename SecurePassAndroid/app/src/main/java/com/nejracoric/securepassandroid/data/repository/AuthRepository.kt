package com.nejracoric.securepassandroid.data.repository

import com.nejracoric.securepassandroid.data.api.LoginRequest
import com.nejracoric.securepassandroid.data.api.RegisterRequest
import com.nejracoric.securepassandroid.data.api.RetrofitClient
import com.nejracoric.securepassandroid.data.local.TokenManager
import retrofit2.HttpException
import java.io.IOException

class AuthRepository(
    private val tokenManager: TokenManager,
) {

    private val api = RetrofitClient.api

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val response = api.login(LoginRequest(email.trim().lowercase(), password))
        tokenManager.saveSession(
            token = response.token,
            userId = response.user.id,
            email = response.user.email,
        )
    }.mapError()

    suspend fun register(email: String, password: String): Result<Unit> = runCatching {
        api.register(RegisterRequest(email.trim().lowercase(), password))
        login(email, password).getOrThrow()
    }.mapError()

    suspend fun logout() {
        tokenManager.clearSession()
    }

    suspend fun hasSession(): Boolean = tokenManager.hasSession()

    suspend fun getStoredEmail(): String? = tokenManager.getEmail()

    private fun <T> Result<T>.mapError(): Result<T> {
        return fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                val message = when (error) {
                    is HttpException -> {
                        error.response()?.errorBody()?.string()
                            ?.let { body ->
                                runCatching {
                                    com.google.gson.Gson().fromJson(body, com.nejracoric.securepassandroid.data.api.ApiError::class.java).message
                                }.getOrNull()
                            } ?: "Greška servera (${error.code()})"
                    }
                    is IOException -> "Nema konekcije sa serverom. Provjeri da li backend radi."
                    else -> error.message ?: "Nepoznata greška"
                }
                Result.failure(Exception(message))
            },
        )
    }
}
