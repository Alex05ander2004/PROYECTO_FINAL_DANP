@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.refood.data.repository

import com.example.refood.data.remote.CartApi
import com.example.refood.data.remote.dto.AddCartItemRequest
import com.example.refood.data.remote.dto.CartItemDto
import com.example.refood.data.remote.dto.UpdateCartItemRequest
import com.example.refood.domain.model.CartLine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * REST no empuja cambios solo: cada mutacion emite a [refreshTrigger] para que las
 * pantallas suscritas a observeCart() vuelvan a pedir el carrito al instante,
 * imitando el comportamiento reactivo que Room daba gratis con sus Flow.
 */
class RemoteCartRepositoryImpl(
    private val cartApi: CartApi,
    private val productRepository: ProductRepository
) : CartRepository {

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    override fun observeCart(userId: Long): Flow<List<CartLine>> =
        refreshTrigger.flatMapLatest { flow { emit(fetchCartLines()) } }

    override fun observeCartItemCount(userId: Long): Flow<Int> =
        observeCart(userId).map { lines -> lines.sumOf { it.quantity } }

    override suspend fun addToCart(userId: Long, productId: Long, quantity: Int) {
        cartApi.addItem(AddCartItemRequest(product = productId, quantity = quantity))
        refreshTrigger.tryEmit(Unit)
    }

    override suspend fun updateQuantity(cartItemId: Long, quantity: Int) {
        if (quantity <= 0) {
            removeItem(cartItemId)
            return
        }
        cartApi.updateQuantity(cartItemId, UpdateCartItemRequest(quantity))
        refreshTrigger.tryEmit(Unit)
    }

    override suspend fun removeItem(cartItemId: Long) {
        cartApi.removeItem(cartItemId)
        refreshTrigger.tryEmit(Unit)
    }

    override suspend fun clearCart(userId: Long) {
        fetchCart().forEach { cartApi.removeItem(it.id) }
        refreshTrigger.tryEmit(Unit)
    }

    private suspend fun fetchCart(): List<CartItemDto> = cartApi.getCart()

    private suspend fun fetchCartLines(): List<CartLine> =
        fetchCart().mapNotNull { item ->
            val product = productRepository.getById(item.product) ?: return@mapNotNull null
            CartLine(cartItemId = item.id, product = product, quantity = item.quantity)
        }
}
