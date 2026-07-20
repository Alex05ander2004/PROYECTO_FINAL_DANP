package com.example.refood.data.repository

import com.example.refood.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: Flow<Long?>

    suspend fun register(
        name: String,
        email: String,
        password: String,
        phone: String,
        address: String
    ): Result<Long>

    suspend fun login(email: String, password: String): Result<Long>

    suspend fun logout()

    suspend fun getUser(userId: Long): User?

    suspend fun updateUser(user: User): Result<Unit>
}
