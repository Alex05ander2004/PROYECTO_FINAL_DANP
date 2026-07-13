package com.example.refood.domain.model

data class CartLine(
    val cartItemId: Long,
    val product: Product,
    val quantity: Int
) {
    val lineTotal: Double get() = product.effectivePrice * quantity
}
