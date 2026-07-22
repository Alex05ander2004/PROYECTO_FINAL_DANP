package com.example.refood.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProductDto(
    val id: Long,
    val name: String,
    val description: String,
    val category: String,
    val price: Double,
    @SerializedName("discount_percentage") val discountPercentage: Int?,
    @SerializedName("discount_price") val discountPrice: Double?,
    val image: String,
    val unit: String,
    val stock: Int,
    @SerializedName("expiration_date") val expirationDate: String,
    @SerializedName("is_featured_offer") val isFeaturedOffer: Boolean,
    @SerializedName("is_active") val isActive: Boolean
)
