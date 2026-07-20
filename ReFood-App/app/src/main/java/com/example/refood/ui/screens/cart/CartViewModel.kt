@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.refood.ui.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.refood.data.repository.AuthRepository
import com.example.refood.data.repository.CartRepository
import com.example.refood.domain.model.CartLine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartUiState(
    val lines: List<CartLine> = emptyList(),
    val total: Double = 0.0,
    val isLoading: Boolean = true
)

class CartViewModel(
    authRepository: AuthRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val userIdFlow = authRepository.currentUserId.filterNotNull()

    val uiState: StateFlow<CartUiState> = userIdFlow
        .flatMapLatest { userId -> cartRepository.observeCart(userId) }
        .map { lines -> CartUiState(lines = lines, total = lines.sumOf { it.lineTotal }, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CartUiState())

    fun increment(line: CartLine) {
        val maxStock = line.product.stock.coerceAtLeast(1)
        val newQuantity = (line.quantity + 1).coerceAtMost(maxStock)
        if (newQuantity == line.quantity) return
        viewModelScope.launch { cartRepository.updateQuantity(line.cartItemId, newQuantity) }
    }

    fun decrement(line: CartLine) {
        viewModelScope.launch { cartRepository.updateQuantity(line.cartItemId, line.quantity - 1) }
    }

    fun remove(line: CartLine) {
        viewModelScope.launch { cartRepository.removeItem(line.cartItemId) }
    }
}
