package com.example.refood.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OrderItemDto(
    val id: Long,
    val product: Long?,
    @SerializedName("product_name") val productName: String,
    @SerializedName("unit_price") val unitPrice: Double,
    val quantity: Int
)

data class OrderDto(
    val id: Long,
    val status: String,
    @SerializedName("delivery_address") val deliveryAddress: String,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("payment_reference") val paymentReference: String?,
    val notes: String,
    val total: Double,
    @SerializedName("created_at") val createdAt: String,
    val items: List<OrderItemDto>
)

data class CreateOrderRequest(
    @SerializedName("delivery_address") val deliveryAddress: String,
    @SerializedName("payment_method") val paymentMethod: String,
    @SerializedName("payment_reference") val paymentReference: String?,
    val notes: String
)
