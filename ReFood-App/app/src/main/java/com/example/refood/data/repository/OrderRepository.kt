package com.example.refood.data.repository

import com.example.refood.domain.model.CartLine
import com.example.refood.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun placeOrder(
        userId: Long,
        deliveryAddress: String,
        paymentMethod: String,
        notes: String,
        lines: List<CartLine>,
        paymentReference: String? = null
    ): Result<Long>

    fun observeOrdersForUser(userId: Long): Flow<List<Order>>
    fun observeOrderDetail(orderId: Long): Flow<Order?>
}
