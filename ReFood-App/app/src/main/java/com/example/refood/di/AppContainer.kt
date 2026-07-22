package com.example.refood.di

import android.content.Context
import com.example.refood.data.remote.NetworkModule
import com.example.refood.data.repository.AuthRepository
import com.example.refood.data.repository.CartRepository
import com.example.refood.data.repository.OrderRepository
import com.example.refood.data.repository.ProductRepository
import com.example.refood.data.repository.RemoteAuthRepositoryImpl
import com.example.refood.data.repository.RemoteCartRepositoryImpl
import com.example.refood.data.repository.RemoteOrderRepositoryImpl
import com.example.refood.data.repository.RemoteProductRepositoryImpl
import com.example.refood.data.session.SessionManager

/**
 * Contenedor manual de dependencias (service locator liviano). Sustituye a un framework de
 * inyección (Hilt/Koin) para mantener la build simple; migrar es directo si el proyecto crece.
 */
class AppContainer(context: Context) {

    private val sessionManager = SessionManager(context)
    val networkModule = NetworkModule(sessionManager)

    val authRepository: AuthRepository = RemoteAuthRepositoryImpl(networkModule.authApi, sessionManager)
    val productRepository: ProductRepository = RemoteProductRepositoryImpl(networkModule.productApi)
    val cartRepository: CartRepository = RemoteCartRepositoryImpl(networkModule.cartApi, productRepository)
    val orderRepository: OrderRepository = RemoteOrderRepositoryImpl(networkModule.orderApi)
}
