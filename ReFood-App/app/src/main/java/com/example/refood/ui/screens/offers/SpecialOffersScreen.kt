package com.example.refood.ui.screens.offers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.refood.ui.components.EmptyState
import com.example.refood.ui.components.LoadingIndicator
import com.example.refood.ui.components.ProductCard
import com.example.refood.ui.components.ReFoodTopBar

@Composable
fun SpecialOffersScreen(
    viewModel: SpecialOffersViewModel,
    onProductClick: (Long) -> Unit,
    onCartClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            ReFoodTopBar(title = "Ofertas especiales", cartCount = uiState.cartCount, onCartClick = onCartClick)
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingIndicator(modifier = Modifier.padding(padding))
            return@Scaffold
        }
        if (uiState.errorMessage != null) {
            EmptyState(
                title = "Sin conexión",
                message = uiState.errorMessage!!,
                icon = Icons.Outlined.WifiOff,
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }
        if (uiState.offers.isEmpty()) {
            EmptyState(
                title = "No hay ofertas por ahora",
                message = "Vuelve más tarde para encontrar nuevos excedentes con descuento.",
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }
        Column(modifier = Modifier.padding(padding)) {
            Text(
                text = "Productos próximos a vencer con descuento especial",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(uiState.offers, key = { it.id }) { product ->
                    ProductCard(product = product, onClick = { onProductClick(product.id) })
                }
            }
        }
    }
}
