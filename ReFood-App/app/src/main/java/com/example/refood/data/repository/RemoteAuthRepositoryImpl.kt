package com.example.refood.data.repository

import com.example.refood.data.remote.AuthApi
import com.example.refood.data.remote.dto.LoginRequest
import com.example.refood.data.remote.dto.RegisterRequest
import com.example.refood.data.session.SessionManager
import com.example.refood.domain.model.User
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.HttpException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Auth real contra el backend Django. El perfil se cachea en DataStore (SessionManager) */
class RemoteAuthRepositoryImpl(
    private val authApi: AuthApi,
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
        sessionManager.cacheUserProfile(me.name, me.email, me.phone, me.address)
        sessionManager.setLoggedInUser(me.id)
        registerFcmTokenBestEffort()
        return me.id
    }

    override suspend fun logout() {
        sessionManager.clearSession()
    }

    override suspend fun getUser(userId: Long): User? {
        val cached = sessionManager.getCachedUserProfile() ?: return null
        return User(id = userId, name = cached.name, email = cached.email, phone = cached.phone, address = cached.address)
    }

    override suspend fun updateUser(user: User): Result<Unit> = try {
        authApi.updateMe(mapOf("name" to user.name, "phone" to user.phone, "address" to user.address))
        sessionManager.cacheUserProfile(user.name, user.email, user.phone, user.address)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e.toFriendlyError())
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

    private fun Exception.toFriendlyError(): Exception = when (this) {
        is HttpException -> when (code()) {
            401 -> IllegalArgumentException("Correo o contraseña incorrectos.")
            400 -> IllegalArgumentException("Ya existe una cuenta con ese correo, o los datos no son válidos.")
            else -> IllegalStateException("No se pudo conectar con el servidor (código ${code()}).")
        }
        else -> IllegalStateException("No se pudo conectar con el servidor. Revisa tu conexión.")
    }
}
