package com.nejracoric.securepassandroid.data.repository

import com.nejracoric.securepassandroid.data.api.CreatePasswordRequest
import com.nejracoric.securepassandroid.data.api.PasswordEntryDto
import com.nejracoric.securepassandroid.data.api.RetrofitClient
import retrofit2.HttpException
import java.io.IOException

class VaultRepository {

    private val api = RetrofitClient.api

    suspend fun getPasswords(): Result<List<PasswordEntryDto>> = runCatching {
        api.getPasswords().passwords
    }.mapError()

    suspend fun createPassword(
        title: String,
        username: String?,
        password: String,
    ): Result<PasswordEntryDto> = runCatching {
        api.createPassword(
            CreatePasswordRequest(
                title = title.trim(),
                username = username?.trim()?.ifBlank { null },
                password = password,
            ),
        ).entry
    }.mapError()

    suspend fun deletePassword(id: String): Result<Unit> = runCatching {
        api.deletePassword(id)
        Unit
    }.mapError()

    private fun <T> Result<T>.mapError(): Result<T> {
        return fold(
            onSuccess = { Result.success(it) },
            onFailure = { error ->
                val message = when (error) {
                    is HttpException -> "Greška servera (${error.code()})"
                    is IOException -> "Nema konekcije sa serverom"
                    else -> error.message ?: "Nepoznata greška"
                }
                Result.failure(Exception(message))
            },
        )
    }
}
