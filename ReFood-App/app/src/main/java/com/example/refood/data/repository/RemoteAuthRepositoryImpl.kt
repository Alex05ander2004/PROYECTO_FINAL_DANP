package com.example.refood.data.repository

import com.example.refood.data.local.dao.UserDao
import com.example.refood.data.local.entity.UserEntity
import com.example.refood.data.remote.AuthApi
import com.example.refood.data.remote.dto.LoginRequest
import com.example.refood.data.remote.dto.RegisterRequest
import com.example.refood.data.remote.dto.UserDto
import com.example.refood.data.session.SessionManager
import com.example.refood.domain.model.User
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.HttpException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Auth real contra el backend Django. Productos/Carrito/Pedidos siguen en
 * Room local (ver plan), por eso cada login/registro hace un "upsert" del
 * usuario en la tabla local `users`: CartItemEntity/OrderEntity tienen FK a
 * ese id, y necesitan una fila local para seguir funcionando.
 */
class RemoteAuthRepositoryImpl(
    private val authApi: AuthApi,
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) : AuthRepository {

    override val currentUserId: Flow<Long?> = sessionManager.currentUserId

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        phone: String,
        address: String
    ): Result<Long> = try {
        authApi.register(RegisterRequest(name, email, password, phone, address))
        Result.success(authenticateAndSync(email, password))
    } catch (e: Exception) {
        Result.failure(e.toFriendlyError())
    }

    override suspend fun login(email: String, password: String): Result<Long> = try {
        Result.success(authenticateAndSync(email, password))
    } catch (e: Exception) {
        Result.failure(e.toFriendlyError())
    }

    private suspend fun authenticateAndSync(email: String, password: String): Long {
        val tokens = authApi.login(LoginRequest(email, password))
        sessionManager.setTokens(tokens.access, tokens.refresh)
        val me = authApi.getMe()
        upsertLocalUser(me)
        sessionManager.setLoggedInUser(me.id)
        registerFcmTokenBestEffort()
        return me.id
    }

    override suspend fun logout() {
        sessionManager.clearSession()
    }

    override suspend fun getUser(userId: Long): User? = userDao.getById(userId)?.toDomain()

    override suspend fun updateUser(user: User): Result<Unit> = try {
        authApi.updateMe(mapOf("name" to user.name, "phone" to user.phone, "address" to user.address))
        val existing = userDao.getById(user.id)
        if (existing != null) {
            userDao.update(existing.copy(name = user.name, phone = user.phone, address = user.address))
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e.toFriendlyError())
    }

    private suspend fun upsertLocalUser(dto: UserDto) {
        val existing = userDao.getById(dto.id)
        val entity = UserEntity(
            id = dto.id,
            name = dto.name,
            email = dto.email,
            // La contraseña real la valida el backend; no se guarda nada local.
            passwordHash = "REMOTE_AUTH",
            phone = dto.phone,
            address = dto.address,
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )
        if (existing != null) userDao.update(entity) else userDao.insert(entity)
    }

    private suspend fun registerFcmTokenBestEffort() {
        runCatching {
            val token = getFcmToken()
            authApi.updateMe(mapOf("fcm_token" to token))
        }
    }

    private suspend fun getFcmToken(): String = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token -> continuation.resume(token) }
            .addOnFailureListener { error -> continuation.resumeWithException(error) }
    }

    private fun UserEntity.toDomain() = User(id = id, name = name, email = email, phone = phone, address = address)

    private fun Exception.toFriendlyError(): Exception = when (this) {
        is HttpException -> when (code()) {
            401 -> IllegalArgumentException("Correo o contraseña incorrectos.")
            400 -> IllegalArgumentException("Ya existe una cuenta con ese correo, o los datos no son válidos.")
            else -> IllegalStateException("No se pudo conectar con el servidor (código ${code()}).")
        }
        else -> IllegalStateException("No se pudo conectar con el servidor. Revisa tu conexión.")
    }
}
