@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.refood.data.repository

import com.example.refood.data.local.dao.CartDao
import com.example.refood.data.local.dao.OrderDao
import com.example.refood.data.local.entity.OrderEntity
import com.example.refood.data.local.entity.OrderItemEntity
import com.example.refood.domain.model.CartLine
import com.example.refood.domain.model.Order
import com.example.refood.domain.model.OrderLine
import com.example.refood.domain.model.OrderStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class OrderRepositoryImpl(
    private val orderDao: OrderDao,
    private val cartDao: CartDao
) : OrderRepository {

    override suspend fun placeOrder(
        userId: Long,
        deliveryAddress: String,
        paymentMethod: String,
        notes: String,
        lines: List<CartLine>,
        paymentReference: String?
    ): Result<Long> {
        if (lines.isEmpty()) {
            return Result.failure(IllegalStateException("El carrito está vacío."))
        }
        val total = lines.sumOf { it.lineTotal }
        val orderId = orderDao.insertOrder(
            OrderEntity(
                userId = userId,
                createdAt = System.currentTimeMillis(),
                status = OrderStatus.PENDIENTE.name,
                deliveryAddress = deliveryAddress.trim(),
                paymentMethod = paymentMethod,
                notes = notes.trim(),
                total = total,
                paymentReference = paymentReference?.trim()?.takeIf { it.isNotBlank() }
            )
        )
        orderDao.insertItems(
            lines.map { line ->
                OrderItemEntity(
                    orderId = orderId,
                    productId = line.product.id,
                    productName = line.product.name,
                    unitPrice = line.product.effectivePrice,
                    quantity = line.quantity
                )
            }
        )
        cartDao.clearForUser(userId)
        return Result.success(orderId)
    }

    override fun observeOrdersForUser(userId: Long): Flow<List<Order>> =
        orderDao.observeOrdersForUser(userId).map { orders -> orders.map { it.toDomain() } }

    override fun observeOrderDetail(orderId: Long): Flow<Order?> =
        orderDao.observeOrderById(orderId).flatMapLatest { order ->
            if (order == null) {
                flowOf(null)
            } else {
                orderDao.observeItemsForOrder(orderId).map { items ->
                    order.toDomain().copy(items = items.map { it.toDomain() })
                }
            }
        }

    private fun OrderEntity.toDomain() = Order(
        id = id,
        createdAt = createdAt,
        status = runCatching { OrderStatus.valueOf(status) }.getOrDefault(OrderStatus.PENDIENTE),
        deliveryAddress = deliveryAddress,
        paymentMethod = paymentMethod,
        notes = notes,
        total = total,
        paymentReference = paymentReference
    )

    private fun OrderItemEntity.toDomain() = OrderLine(
        productId = productId,
        productName = productName,
        unitPrice = unitPrice,
        quantity = quantity
    )
}
