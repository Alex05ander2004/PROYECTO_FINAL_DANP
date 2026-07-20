package com.example.refood.data.repository

import com.example.refood.data.local.dao.UserDao
import com.example.refood.data.local.entity.UserEntity
import com.example.refood.data.session.SessionManager
import com.example.refood.domain.model.User
import kotlinx.coroutines.flow.Flow

class AuthRepositoryImpl(
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
    ): Result<Long> {
        val normalizedEmail = email.trim().lowercase()
        if (userDao.getByEmail(normalizedEmail) != null) {
            return Result.failure(IllegalStateException("Ya existe una cuenta con ese correo."))
        }
        val id = userDao.insert(
            UserEntity(
                name = name.trim(),
                email = normalizedEmail,
                passwordHash = PasswordHasher.hash(password),
                phone = phone.trim(),
                address = address.trim(),
                createdAt = System.currentTimeMillis()
            )
        )
        sessionManager.setLoggedInUser(id)
        return Result.success(id)
    }

    override suspend fun login(email: String, password: String): Result<Long> {
        val user = userDao.getByEmail(email.trim().lowercase())
            ?: return Result.failure(IllegalArgumentException("No existe una cuenta con ese correo."))
        if (user.passwordHash != PasswordHasher.hash(password)) {
            return Result.failure(IllegalArgumentException("Contraseña incorrecta."))
        }
        sessionManager.setLoggedInUser(user.id)
        return Result.success(user.id)
    }

    override suspend fun logout() {
        sessionManager.clearSession()
    }

    override suspend fun getUser(userId: Long): User? =
        userDao.getById(userId)?.toDomain()

    override suspend fun updateUser(user: User): Result<Unit> {
        val existing = userDao.getById(user.id)
            ?: return Result.failure(IllegalStateException("Usuario no encontrado."))
        userDao.update(
            existing.copy(
                name = user.name.trim(),
                phone = user.phone.trim(),
                address = user.address.trim()
            )
        )
        return Result.success(Unit)
    }

    private fun UserEntity.toDomain() = User(
        id = id,
        name = name,
        email = email,
        phone = phone,
        address = address
    )
}
