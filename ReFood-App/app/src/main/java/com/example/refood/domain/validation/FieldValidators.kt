package com.example.refood.domain.validation

import android.util.Patterns
import java.util.Calendar

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

    fun cardNumberError(value: String): String? = when {
        value.isBlank() -> "Ingresa el número de tu tarjeta."
        !value.all { it.isDigit() } -> "El número de tarjeta solo debe contener números."
        value.length !in 13..19 -> "Ingresa un número de tarjeta válido."
        else -> null
    }

    /** Espera formato MM/AA (el ViewModel ya inserta la barra al escribir). */
    fun cardExpiryError(value: String): String? {
        val match = Regex("^(\\d{2})/(\\d{2})$").find(value)
            ?: return "Ingresa el vencimiento en formato MM/AA."
        val (monthStr, yearStr) = match.destructured
        val month = monthStr.toInt()
        if (month !in 1..12) return "Ingresa un mes válido (01-12)."

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR) % 100
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val year = yearStr.toInt()
        val isExpired = year < currentYear || (year == currentYear && month < currentMonth)
        if (isExpired) return "Esta tarjeta ya venció."
        return null
    }

    fun cardCvvError(value: String): String? = when {
        value.isBlank() -> "Ingresa el código de seguridad (CVV)."
        !value.all { it.isDigit() } -> "El CVV solo debe contener números."
        value.length !in 3..4 -> "Ingresa un CVV válido."
        else -> null
    }

    fun cardHolderNameError(name: String): String? = when {
        name.isBlank() -> "Ingresa el nombre del titular de la tarjeta."
        !nameRegex.matches(name.trim()) -> "El nombre solo debe contener letras."
        else -> null
    }
}
