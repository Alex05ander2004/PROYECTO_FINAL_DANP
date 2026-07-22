package com.example.refood.data.repository

import com.example.refood.data.remote.ProductApi
import com.example.refood.data.remote.dto.ProductDto
import com.example.refood.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class RemoteProductRepositoryImpl(private val productApi: ProductApi) : ProductRepository {

    override fun observeAll(): Flow<List<Product>> = flow { emit(fetchAll()) }

    override fun observeOffers(): Flow<List<Product>> =
        flow { emit(fetchAll().filter { it.isFeaturedOffer }) }

    override fun observeCategories(): Flow<List<String>> =
        flow { emit(fetchAll().map { it.category }.distinct().sorted()) }

    override fun observeById(productId: Long): Flow<Product?> =
        flow { emit(getById(productId)) }

    override suspend fun getById(productId: Long): Product? =
        productApi.getById(productId).toDomain()

    private suspend fun fetchAll(): List<Product> = productApi.getAll().map { it.toDomain() }

    private fun ProductDto.toDomain() = Product(
        id = id,
        name = name,
        description = description,
        category = category,
        price = price,
        discountPrice = discountPrice,
        imageUrl = image,
        unit = unit,
        stock = stock,
        expirationDate = expirationDate,
        isFeaturedOffer = isFeaturedOffer
    )
}
