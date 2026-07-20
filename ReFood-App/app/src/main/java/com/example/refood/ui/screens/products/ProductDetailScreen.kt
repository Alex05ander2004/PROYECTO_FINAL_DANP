package com.example.refood.ui.screens.products

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.refood.ui.components.ExpiryLabel
import com.example.refood.ui.components.LoadingIndicator
import com.example.refood.ui.components.OfferBadge
import com.example.refood.ui.components.PrimaryButton
import com.example.refood.ui.components.QuantityStepper
import com.example.refood.ui.components.ReFoodTopBar

@Composable
fun ProductDetailScreen(
    viewModel: ProductDetailViewModel,
    onBack: () -> Unit,
    onCartClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.addedToCart) {
        if (uiState.addedToCart) {
            snackbarHostState.showSnackbar("Producto agregado al carrito")
            viewModel.consumeAddedToCartEvent()
        }
    }

    Scaffold(
        topBar = {
            ReFoodTopBar(
                title = uiState.product?.name ?: "Detalle",
                onBack = onBack,
                cartCount = uiState.cartCount,
                onCartClick = onCartClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val product = uiState.product
        if (uiState.isLoading || product == null) {
            LoadingIndicator(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = product.category, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = product.name, style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (product.discountPrice != null) {
                        Text(
                            text = "S/ %.2f".format(product.price),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "S/ %.2f".format(product.effectivePrice),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if (product.discountPercent != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OfferBadge(percentOff = product.discountPercent!!)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    ExpiryLabel(product = product)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Descripción", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                InfoRow(label = "Presentación", value = product.unit)
                InfoRow(label = "Stock disponible", value = "${product.stock} unidades")
                InfoRow(label = "Fecha de vencimiento", value = product.expirationDateLabel)

                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Cantidad", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                QuantityStepper(
                    quantity = uiState.quantity,
                    onIncrement = viewModel::incrementQuantity,
                    onDecrement = viewModel::decrementQuantity
                )

                Spacer(modifier = Modifier.height(24.dp))
                PrimaryButton(
                    text = "Agregar al carrito · S/ %.2f".format(product.effectivePrice * uiState.quantity),
                    onClick = viewModel::addToCart
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
