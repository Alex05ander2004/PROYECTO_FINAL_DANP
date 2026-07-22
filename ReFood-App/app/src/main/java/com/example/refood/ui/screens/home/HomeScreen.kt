package com.example.refood.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun HomeScreen(
    viewModel: HomeViewModel,
    onProductClick: (Long) -> Unit,
    onSeeAllOffers: () -> Unit,
    onSeeAllProducts: () -> Unit,
    onCartClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            ReFoodTopBar(
                title = "ReFood",
                cartCount = uiState.cartCount,
                onCartClick = onCartClick
            )
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (uiState.userName.isNotBlank()) "Hola, ${uiState.userName.substringBefore(" ")} 👋" else "Hola 👋",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Rescata excedentes de comida a precios accesibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (uiState.offers.isNotEmpty()) {
                item {
                    SectionHeader(title = "Ofertas especiales", onSeeAll = onSeeAllOffers)
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.offers, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                modifier = Modifier.width(160.dp),
                                onClick = { onProductClick(product.id) }
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Productos disponibles", onSeeAll = onSeeAllProducts)
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.recentProducts, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            modifier = Modifier.width(160.dp),
                            onClick = { onProductClick(product.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        TextButton(onClick = onSeeAll) {
            Text("Ver todo")
        }
    }
}
