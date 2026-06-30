package com.nejracoric.securepassandroid.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SecurePassApi {

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): RegisterResponse

    @GET("api/passwords")
    suspend fun getPasswords(): PasswordsResponse

    @POST("api/passwords")
    suspend fun createPassword(@Body body: CreatePasswordRequest): CreatePasswordResponse

    @DELETE("api/passwords/{id}")
    suspend fun deletePassword(@Path("id") id: String): MessageResponse
}
