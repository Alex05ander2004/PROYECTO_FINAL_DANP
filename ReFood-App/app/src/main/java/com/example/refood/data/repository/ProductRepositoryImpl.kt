package com.example.refood.data.repository

import com.example.refood.data.local.dao.ProductDao
import com.example.refood.data.local.entity.ProductEntity
import com.example.refood.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(private val productDao: ProductDao) : ProductRepository {

    override fun observeAll(): Flow<List<Product>> =
        productDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeOffers(): Flow<List<Product>> =
        productDao.observeOffers().map { list -> list.map { it.toDomain() } }

    override fun observeCategories(): Flow<List<String>> = productDao.observeCategories()

    override fun observeById(productId: Long): Flow<Product?> =
        productDao.observeById(productId).map { it?.toDomain() }

    override suspend fun getById(productId: Long): Product? =
        productDao.getById(productId)?.toDomain()

    private fun ProductEntity.toDomain() = Product(
        id = id,
        name = name,
        description = description,
        category = category,
        price = price,
        discountPrice = discountPrice,
        imageUrl = imageUrl,
        unit = unit,
        stock = stock,
        expirationDate = expirationDate,
        isFeaturedOffer = isFeaturedOffer
    )
}
