package com.example.refood.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
)
