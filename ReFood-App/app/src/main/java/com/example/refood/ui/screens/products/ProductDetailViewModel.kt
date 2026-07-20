@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.refood.ui.screens.products

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.refood.data.repository.AuthRepository
import com.example.refood.data.repository.CartRepository
import com.example.refood.data.repository.ProductRepository
import com.example.refood.domain.model.Product
import com.example.refood.navigation.Screen
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val product: Product? = null,
    val quantity: Int = 1,
    val cartCount: Int = 0,
    val isLoading: Boolean = true,
    val addedToCart: Boolean = false
)

class ProductDetailViewModel(
    savedStateHandle: SavedStateHandle,
    authRepository: AuthRepository,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val productId: Long =
        checkNotNull(savedStateHandle.get<String>(Screen.ProductDetail.ARG_PRODUCT_ID)).toLong()

    private val quantity = MutableStateFlow(1)
    private val addedToCart = MutableStateFlow(false)
    private val userIdFlow = authRepository.currentUserId.filterNotNull()
    private val cartCountFlow = userIdFlow.flatMapLatest { cartRepository.observeCartItemCount(it) }

    val uiState: StateFlow<ProductDetailUiState> = combine(
        productRepository.observeById(productId),
        quantity,
        cartCountFlow,
        addedToCart
    ) { product, qty, cartCount, added ->
        ProductDetailUiState(
            product = product,
            quantity = qty,
            cartCount = cartCount,
            isLoading = product == null,
            addedToCart = added
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductDetailUiState())

    fun incrementQuantity() {
        val maxStock = uiState.value.product?.stock ?: Int.MAX_VALUE
        quantity.update { (it + 1).coerceAtMost(maxStock.coerceAtLeast(1)) }
    }

    fun decrementQuantity() {
        quantity.update { (it - 1).coerceAtLeast(1) }
    }

    fun addToCart() {
        viewModelScope.launch {
            val userId = userIdFlow.first()
            cartRepository.addToCart(userId, productId, quantity.value)
            addedToCart.value = true
        }
    }

    fun consumeAddedToCartEvent() {
        addedToCart.value = false
    }
}
