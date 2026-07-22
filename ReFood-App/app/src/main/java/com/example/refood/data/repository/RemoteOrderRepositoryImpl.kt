@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.refood.data.repository

import com.example.refood.data.remote.OrderApi
import com.example.refood.data.remote.dto.CreateOrderRequest
import com.example.refood.data.remote.dto.OrderDto
import com.example.refood.data.remote.dto.OrderItemDto
import com.example.refood.domain.model.CartLine
import com.example.refood.domain.model.Order
import com.example.refood.domain.model.OrderLine
import com.example.refood.domain.model.OrderStatus
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class RemoteOrderRepositoryImpl(private val orderApi: OrderApi) : OrderRepository {

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    override suspend fun placeOrder(
        userId: Long,
        deliveryAddress: String,
        paymentMethod: String,
        notes: String,
        lines: List<CartLine>,
        paymentReference: String?
    ): Result<Long> = runCatching {
        val order = orderApi.checkout(
            CreateOrderRequest(
                deliveryAddress = deliveryAddress.trim(),
                paymentMethod = paymentMethod.uppercase(),
                paymentReference = paymentReference?.trim()?.takeIf { it.isNotBlank() },
                notes = notes.trim()
            )
        )
        refreshTrigger.tryEmit(Unit)
        order.id
    }

    override fun observeOrdersForUser(userId: Long): Flow<List<Order>> =
        refreshTrigger.flatMapLatest { flow { emit(orderApi.getAll().map { it.toDomain() }) } }

    override fun observeOrderDetail(orderId: Long): Flow<Order?> =
        refreshTrigger.flatMapLatest { flow { emit(orderApi.getById(orderId).toDomain()) } }

    private fun OrderDto.toDomain() = Order(
        id = id,
        createdAt = parseIsoDateToMillis(createdAt),
        status = runCatching { OrderStatus.valueOf(status) }.getOrDefault(OrderStatus.PENDIENTE),
        deliveryAddress = deliveryAddress,
        paymentMethod = paymentMethod.lowercase().replaceFirstChar { it.uppercase() },
        notes = notes,
        total = total,
        paymentReference = paymentReference,
        items = items.map { it.toDomain() }
    )

    private fun OrderItemDto.toDomain() = OrderLine(
        productId = product ?: 0L,
        productName = productName,
        unitPrice = unitPrice,
        quantity = quantity
    )

    private fun parseIsoDateToMillis(isoDate: String): Long = runCatching {
        val cleaned = isoDate.substringBefore(".").substringBefore("Z") + "Z"
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("UTC")
        }
        format.parse(cleaned)!!.time
    }.getOrDefault(System.currentTimeMillis())
}
