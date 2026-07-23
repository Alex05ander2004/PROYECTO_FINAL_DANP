package com.example.refood.ui.screens.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.ui.unit.dp
import com.example.refood.ui.components.EmptyState
import com.example.refood.ui.components.LoadingIndicator
import com.example.refood.ui.components.ReFoodTopBar
import com.example.refood.ui.components.StatusChip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderDetailScreen(
    viewModel: OrderDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es", "PE")) }

    Scaffold(
        topBar = { ReFoodTopBar(title = "Detalle del pedido", onBack = onBack) }
    ) { padding ->
        val order = uiState.order
        if (uiState.errorMessage != null) {
            EmptyState(
                title = "Sin conexión",
                message = uiState.errorMessage!!,
                icon = Icons.Outlined.WifiOff,
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }
        if (uiState.isLoading || order == null) {
            LoadingIndicator(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Pedido #${order.id}", style = MaterialTheme.typography.headlineSmall)
                StatusChip(status = order.status)
            }
            Text(
                text = dateFormatter.format(Date(order.createdAt)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Productos", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            order.items.forEach { line ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "${line.quantity}x ${line.productName}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "S/ %.2f".format(line.lineTotal), style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Total", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "S/ %.2f".format(order.total),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Entrega", style = MaterialTheme.typography.titleMedium)
            Text(text = order.deliveryAddress, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Método de pago", style = MaterialTheme.typography.titleMedium)
            Text(text = order.paymentMethod, style = MaterialTheme.typography.bodyMedium)

            if (!order.paymentReference.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (order.paymentMethod == "Tarjeta") {
                        "Tarjeta terminada en ${order.paymentReference}"
                    } else {
                        "N° de operación: ${order.paymentReference}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (order.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Notas", style = MaterialTheme.typography.titleMedium)
                Text(text = order.notes, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
