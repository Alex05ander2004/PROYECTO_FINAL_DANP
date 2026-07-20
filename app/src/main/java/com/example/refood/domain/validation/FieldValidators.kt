package com.example.refood.domain.validation

import android.util.Patterns

/** Reglas de validación compartidas por Registro y Perfil (mismos campos, mismas reglas). */
object FieldValidators {

    private val nameRegex = Regex("^[\\p{L} '.-]+$")

    fun nameError(name: String): String? = when {
        name.isBlank() -> "Ingresa tu nombre completo."
        !nameRegex.matches(name.trim()) -> "El nombre solo debe contener letras."
        else -> null
    }

    fun emailError(email: String): String? = when {
        email.isBlank() -> "Ingresa tu correo electrónico."
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Ingresa un correo válido, ej. nombre@gmail.com."
        else -> null
    }

    fun phoneError(phone: String): String? = when {
        phone.isBlank() -> "Ingresa tu número de teléfono."
        !phone.all { it.isDigit() } -> "El teléfono solo debe contener números."
        phone.length !in 7..9 -> "Ingresa un número de teléfono válido."
        else -> null
    }

    fun addressError(address: String): String? = when {
        address.isBlank() -> "Ingresa tu dirección."
        address.trim().length < 5 -> "Ingresa una dirección más específica."
        else -> null
    }

    fun passwordError(password: String): String? = when {
        password.isBlank() || password.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
        else -> null
    }

    fun operationNumberError(value: String): String? = when {
        value.isBlank() -> "Ingresa el número de operación de tu pago."
        !value.all { it.isDigit() } -> "El número de operación solo debe contener números."
        value.length !in 6..10 -> "Ingresa un número de operación válido."
        else -> null
    }
}
