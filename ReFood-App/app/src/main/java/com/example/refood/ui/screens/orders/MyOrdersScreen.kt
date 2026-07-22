package com.example.refood.ui.screens.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.refood.domain.model.Order
import com.example.refood.ui.components.EmptyState
import com.example.refood.ui.components.LoadingIndicator
import com.example.refood.ui.components.ReFoodTopBar
import com.example.refood.ui.components.StatusChip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyOrdersScreen(
    viewModel: MyOrdersViewModel,
    onOrderClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { ReFoodTopBar(title = "Mis pedidos") }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingIndicator(modifier = Modifier.padding(padding))
            uiState.errorMessage != null -> EmptyState(
                title = "Sin conexión",
                message = uiState.errorMessage!!,
                icon = Icons.Outlined.WifiOff,
                modifier = Modifier.padding(padding)
            )
            uiState.orders.isEmpty() -> EmptyState(
                title = "Aún no tienes pedidos",
                message = "Cuando hagas tu primer pedido, lo verás reflejado aquí.",
                modifier = Modifier.padding(padding)
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.orders, key = { it.id }) { order ->
                    OrderSummaryRow(order = order, onClick = { onOrderClick(order.id) })
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun OrderSummaryRow(order: Order, onClick: () -> Unit) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("es", "PE")) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Pedido #${order.id}", style = MaterialTheme.typography.titleMedium)
            StatusChip(status = order.status)
        }
        Text(
            text = dateFormatter.format(Date(order.createdAt)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = order.deliveryAddress, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            Text(
                text = "S/ %.2f".format(order.total),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
