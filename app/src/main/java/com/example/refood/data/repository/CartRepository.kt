package com.example.refood.data.repository

import com.example.refood.domain.model.CartLine
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun observeCart(userId: Long): Flow<List<CartLine>>
    fun observeCartItemCount(userId: Long): Flow<Int>
    suspend fun addToCart(userId: Long, productId: Long, quantity: Int = 1)
    suspend fun updateQuantity(cartItemId: Long, quantity: Int)
    suspend fun removeItem(cartItemId: Long)
    suspend fun clearCart(userId: Long)
}
