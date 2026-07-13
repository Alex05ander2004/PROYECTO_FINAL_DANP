package com.example.refood.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.refood.domain.model.OrderStatus
import com.example.refood.ui.theme.StatusCancelled
import com.example.refood.ui.theme.StatusDelivered
import com.example.refood.ui.theme.StatusPending
import com.example.refood.ui.theme.StatusPreparing
import com.example.refood.ui.theme.StatusReady

@Composable
fun StatusChip(status: OrderStatus, modifier: Modifier = Modifier) {
    val color = when (status) {
        OrderStatus.PENDIENTE -> StatusPending
        OrderStatus.EN_PREPARACION -> StatusPreparing
        OrderStatus.LISTO -> StatusReady
        OrderStatus.ENTREGADO -> StatusDelivered
        OrderStatus.CANCELADO -> StatusCancelled
    }
    Text(
        text = status.label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        modifier = modifier
            .background(color, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
