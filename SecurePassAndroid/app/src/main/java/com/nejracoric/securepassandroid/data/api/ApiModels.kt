package com.nejracoric.securepassandroid.data.api

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String,
)

data class RegisterRequest(
    val email: String,
    val password: String,
)

data class UserDto(
    val id: String,
    val email: String,
)

data class LoginResponse(
    val token: String,
    val user: UserDto,
)

data class RegisterResponse(
    val message: String,
    val user: UserDto,
)

data class PasswordsResponse(
    val passwords: List<PasswordEntryDto>,
)

data class PasswordEntryDto(
    val id: String,
    val title: String,
    val username: String?,
    val password: String,
    val url: String?,
    val createdAt: String?,
    val updatedAt: String?,
)

data class CreatePasswordRequest(
    val title: String,
    val username: String?,
    val password: String,
    val url: String? = null,
)

data class CreatePasswordResponse(
    val message: String,
    @SerializedName("password") val entry: PasswordEntryDto,
)

data class MessageResponse(
    val message: String,
)

data class ApiError(
    val message: String?,
)
