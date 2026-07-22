package com.example.refood.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val phone: String,
    val address: String
)

data class LoginRequest(val email: String, val password: String)

data class RefreshRequest(val refresh: String)

data class TokenResponse(val access: String, val refresh: String)

data class RefreshResponse(val access: String)

data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val role: String,
    @SerializedName("fcm_token") val fcmToken: String?
)
