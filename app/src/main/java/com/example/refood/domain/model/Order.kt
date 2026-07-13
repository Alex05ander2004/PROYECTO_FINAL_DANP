package com.example.refood.domain.model

enum class OrderStatus(val label: String) {
    PENDIENTE("Pendiente"),
    EN_PREPARACION("En preparación"),
    LISTO("Listo para entrega"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado")
}

data class OrderLine(
    val productId: Long,
    val productName: String,
    val unitPrice: Double,
    val quantity: Int
) {
    val lineTotal: Double get() = unitPrice * quantity
}

data class Order(
    val id: Long,
    val createdAt: Long,
    val status: OrderStatus,
    val deliveryAddress: String,
    val paymentMethod: String,
    val notes: String,
    val total: Double,
    val items: List<OrderLine> = emptyList()
)
