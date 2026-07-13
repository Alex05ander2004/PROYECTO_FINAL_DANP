package com.example.refood.domain.model

data class Product(
    val id: Long,
    val name: String,
    val description: String,
    val category: String,
    val price: Double,
    val discountPrice: Double?,
    val imageUrl: String,
    val unit: String,
    val stock: Int,
    val expirationDate: String,
    val isFeaturedOffer: Boolean
) {
    val effectivePrice: Double get() = discountPrice ?: price

    val discountPercent: Int?
        get() = discountPrice?.let { dp ->
            if (price <= 0.0) null else (100 - (dp / price * 100)).toInt()
        }
}
