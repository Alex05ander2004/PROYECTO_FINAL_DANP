package com.example.refood.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.refood.domain.model.OrderStatus
import com.example.refood.ui.theme.StatusCancelled
import com.example.refood.ui.theme.StatusCancelledDark
import com.example.refood.ui.theme.StatusDelivered
import com.example.refood.ui.theme.StatusDeliveredDark
import com.example.refood.ui.theme.StatusPending
import com.example.refood.ui.theme.StatusPendingDark
import com.example.refood.ui.theme.StatusPreparing
import com.example.refood.ui.theme.StatusPreparingDark
import com.example.refood.ui.theme.StatusReady
import com.example.refood.ui.theme.StatusReadyDark

@Composable
fun StatusChip(status: OrderStatus, modifier: Modifier = Modifier) {
    val dark = isSystemInDarkTheme()
    val color = when (status) {
        OrderStatus.PENDIENTE -> if (dark) StatusPendingDark else StatusPending
        OrderStatus.EN_PREPARACION -> if (dark) StatusPreparingDark else StatusPreparing
        OrderStatus.LISTO -> if (dark) StatusReadyDark else StatusReady
        OrderStatus.ENTREGADO -> if (dark) StatusDeliveredDark else StatusDelivered
        OrderStatus.CANCELADO -> if (dark) StatusCancelledDark else StatusCancelled
    }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
