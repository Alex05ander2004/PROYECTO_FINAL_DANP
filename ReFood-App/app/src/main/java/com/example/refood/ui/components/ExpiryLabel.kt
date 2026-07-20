package com.example.refood.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.refood.domain.model.ExpiryUrgency
import com.example.refood.domain.model.Product

private fun expiryText(product: Product): String? {
    val days = product.daysUntilExpiration
    return when (product.expiryUrgency) {
        ExpiryUrgency.EXPIRED -> "Vencido"
        ExpiryUrgency.TODAY -> "Vence hoy"
        ExpiryUrgency.SOON -> if (days == 1L) "Vence mañana" else "Vence en $days días"
        ExpiryUrgency.NORMAL -> null
    }
}

/** Chip de urgencia por vencimiento. No dibuja nada si el producto no está próximo a vencer. */
@Composable
fun ExpiryLabel(product: Product, modifier: Modifier = Modifier) {
    val text = expiryText(product) ?: return
    val color = when (product.expiryUrgency) {
        ExpiryUrgency.EXPIRED -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.secondary
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f), RoundedCornerShape(6.dp))
            .border(1.dp, color, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    )
}
