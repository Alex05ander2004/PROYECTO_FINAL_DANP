package com.example.refood.data.repository

import com.example.refood.data.local.dao.CartDao
import com.example.refood.data.local.dao.ProductDao
import com.example.refood.data.local.entity.CartItemEntity
import com.example.refood.domain.model.CartLine
import com.example.refood.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class CartRepositoryImpl(
    private val cartDao: CartDao,
    private val productDao: ProductDao
) : CartRepository {

    override fun observeCart(userId: Long): Flow<List<CartLine>> =
        combine(cartDao.observeCart(userId), productDao.observeAll()) { cartItems, products ->
            val productsById = products.associateBy { it.id }
            cartItems.mapNotNull { item ->
                val entity = productsById[item.productId] ?: return@mapNotNull null
                CartLine(
                    cartItemId = item.id,
                    quantity = item.quantity,
                    product = Product(
                        id = entity.id,
                        name = entity.name,
                        description = entity.description,
                        category = entity.category,
                        price = entity.price,
                        discountPrice = entity.discountPrice,
                        imageUrl = entity.imageUrl,
                        unit = entity.unit,
                        stock = entity.stock,
                        expirationDate = entity.expirationDate,
                        isFeaturedOffer = entity.isFeaturedOffer
                    )
                )
            }
        }

    override fun observeCartItemCount(userId: Long): Flow<Int> =
        cartDao.observeCart(userId).map { items -> items.sumOf { it.quantity } }

    override suspend fun addToCart(userId: Long, productId: Long, quantity: Int) {
        val maxStock = (productDao.getById(productId)?.stock ?: Int.MAX_VALUE).coerceAtLeast(1)
        val existing = cartDao.getItem(userId, productId)
        if (existing != null) {
            val newQuantity = (existing.quantity + quantity).coerceAtMost(maxStock)
            cartDao.update(existing.copy(quantity = newQuantity))
        } else {
            cartDao.insert(CartItemEntity(userId = userId, productId = productId, quantity = quantity.coerceAtMost(maxStock)))
        }
    }

    override suspend fun updateQuantity(cartItemId: Long, quantity: Int) {
        if (quantity <= 0) {
            removeItem(cartItemId)
            return
        }
        val existing = cartDao.getById(cartItemId) ?: return
        val maxStock = (productDao.getById(existing.productId)?.stock ?: Int.MAX_VALUE).coerceAtLeast(1)
        cartDao.update(existing.copy(quantity = quantity.coerceAtMost(maxStock)))
    }

    override suspend fun removeItem(cartItemId: Long) {
        cartDao.deleteById(cartItemId)
    }

    override suspend fun clearCart(userId: Long) {
        cartDao.clearForUser(userId)
    }
}
