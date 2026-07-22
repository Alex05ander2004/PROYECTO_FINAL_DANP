package com.example.refood.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CartItemDto(
    val id: Long,
    val product: Long,
    @SerializedName("product_name") val productName: String,
    @SerializedName("product_price") val productPrice: Double,
    @SerializedName("product_discount_price") val productDiscountPrice: Double?,
    @SerializedName("product_image") val productImage: String?,
    val quantity: Int
)

data class AddCartItemRequest(val product: Long, val quantity: Int)

data class UpdateCartItemRequest(val quantity: Int)
