package com.example.refood.data.repository

import java.security.MessageDigest

/**
 * Hash simple para la demo local (Room). Cuando exista un backend real, la
 * autenticación y el hashing de contraseñas deben moverse al servidor.
 */
object PasswordHasher {
    fun hash(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString(separator = "") { "%02x".format(it) }
    }
}
