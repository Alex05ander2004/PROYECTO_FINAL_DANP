package com.example.refood.di

import android.content.Context
import com.example.refood.data.local.AppDatabase
import com.example.refood.data.remote.NetworkModule
import com.example.refood.data.repository.AuthRepository
import com.example.refood.data.repository.CartRepository
import com.example.refood.data.repository.CartRepositoryImpl
import com.example.refood.data.repository.OrderRepository
import com.example.refood.data.repository.OrderRepositoryImpl
import com.example.refood.data.repository.ProductRepository
import com.example.refood.data.repository.ProductRepositoryImpl
import com.example.refood.data.repository.RemoteAuthRepositoryImpl
import com.example.refood.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope

/**
 * Contenedor manual de dependencias (service locator liviano). Sustituye a un framework de
 * inyección (Hilt/Koin) para mantener la build simple; migrar es directo si el proyecto crece.
 */
class AppContainer(context: Context, applicationScope: CoroutineScope) {

    private val database: AppDatabase = AppDatabase.getInstance(context, applicationScope)
    private val sessionManager = SessionManager(context)
    val networkModule = NetworkModule(sessionManager)

    // Auth habla con el backend real; Productos/Carrito/Pedidos siguen en Room
    // local hasta que se conecten en una siguiente etapa.
    val authRepository: AuthRepository =
        RemoteAuthRepositoryImpl(networkModule.authApi, database.userDao(), sessionManager)
    val productRepository: ProductRepository = ProductRepositoryImpl(database.productDao())
    val cartRepository: CartRepository = CartRepositoryImpl(database.cartDao(), database.productDao())
    val orderRepository: OrderRepository = OrderRepositoryImpl(database.orderDao(), database.cartDao())
}
