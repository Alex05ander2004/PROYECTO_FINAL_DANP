package com.example.refood.data.remote

import com.example.refood.data.remote.dto.LoginRequest
import com.example.refood.data.remote.dto.RefreshRequest
import com.example.refood.data.remote.dto.RefreshResponse
import com.example.refood.data.remote.dto.RegisterRequest
import com.example.refood.data.remote.dto.TokenResponse
import com.example.refood.data.remote.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register/")
    suspend fun register(@Body body: RegisterRequest): UserDto

    @POST("auth/login/")
    suspend fun login(@Body body: LoginRequest): TokenResponse

    @POST("auth/refresh/")
    suspend fun refresh(@Body body: RefreshRequest): RefreshResponse

    @GET("auth/me/")
    suspend fun getMe(): UserDto

    @PATCH("auth/me/")
    suspend fun updateMe(@Body fields: Map<String, @JvmSuppressWildcards Any?>): UserDto
}
