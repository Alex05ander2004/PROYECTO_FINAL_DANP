package com.example.refood.data.repository

import com.example.refood.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeAll(): Flow<List<Product>>
    fun observeOffers(): Flow<List<Product>>
    fun observeCategories(): Flow<List<String>>
    fun observeById(productId: Long): Flow<Product?>
    suspend fun getById(productId: Long): Product?
}
