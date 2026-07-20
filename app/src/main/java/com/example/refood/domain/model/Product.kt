package com.example.refood.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExpiryUrgency { EXPIRED, TODAY, SOON, NORMAL }

data class Product(
    val id: Long,
    val name: String,
    val description: String,
    val category: String,
    val price: Double,
    val discountPrice: Double?,
    val imageUrl: String,
    val unit: String,
    val stock: Int,
    val expirationDate: String,
    val isFeaturedOffer: Boolean
) {
    val effectivePrice: Double get() = discountPrice ?: price

    val discountPercent: Int?
        get() = discountPrice?.let { dp ->
            if (price <= 0.0) null else (100 - (dp / price * 100)).toInt()
        }

    /** Días calendario hasta [expirationDate] (negativo si ya venció). Null si la fecha no se pudo leer. */
    val daysUntilExpiration: Long?
        get() = runCatching {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
            val expiry = format.parse(expirationDate) ?: return@runCatching null
            val today = format.parse(format.format(Date()))!!
            (expiry.time - today.time) / 86_400_000L
        }.getOrNull()

    val expiryUrgency: ExpiryUrgency
        get() = when (val days = daysUntilExpiration) {
            null -> ExpiryUrgency.NORMAL
            else -> when {
                days < 0L -> ExpiryUrgency.EXPIRED
                days == 0L -> ExpiryUrgency.TODAY
                days <= 2L -> ExpiryUrgency.SOON
                else -> ExpiryUrgency.NORMAL
            }
        }

    /** Fecha de vencimiento en formato legible, p.ej. "13 jul 2026". */
    val expirationDateLabel: String
        get() = runCatching {
            val input = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val output = SimpleDateFormat("d MMM yyyy", Locale("es", "PE"))
            output.format(input.parse(expirationDate)!!)
        }.getOrDefault(expirationDate)
}
