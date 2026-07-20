package com.example.refood.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.refood.ui.screens.auth.LoginViewModel
import com.example.refood.ui.screens.auth.RegisterViewModel
import com.example.refood.ui.screens.auth.SplashViewModel
import com.example.refood.ui.screens.cart.CartViewModel
import com.example.refood.ui.screens.home.HomeViewModel
import com.example.refood.ui.screens.offers.SpecialOffersViewModel
import com.example.refood.ui.screens.order.OrderFormViewModel
import com.example.refood.ui.screens.orders.MyOrdersViewModel
import com.example.refood.ui.screens.orders.OrderDetailViewModel
import com.example.refood.ui.screens.products.ProductDetailViewModel
import com.example.refood.ui.screens.products.ProductsViewModel
import com.example.refood.ui.screens.profile.ProfileViewModel

/**
 * Factory único para todos los ViewModels de la app, respaldado por el [AppContainer].
 * Evita depender de un framework de DI mientras el proyecto se mantiene en un solo módulo.
 */
fun appViewModelFactory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
    initializer { SplashViewModel(container.authRepository) }
    initializer { LoginViewModel(container.authRepository) }
    initializer { RegisterViewModel(container.authRepository) }

    initializer {
        HomeViewModel(container.authRepository, container.productRepository, container.cartRepository)
    }
    initializer {
        ProductsViewModel(container.authRepository, container.productRepository, container.cartRepository)
    }
    initializer {
        ProductDetailViewModel(
            createSavedStateHandle(),
            container.authRepository,
            container.productRepository,
            container.cartRepository
        )
    }
    initializer {
        SpecialOffersViewModel(container.authRepository, container.productRepository, container.cartRepository)
    }
    initializer { CartViewModel(container.authRepository, container.cartRepository) }
    initializer {
        OrderFormViewModel(container.authRepository, container.cartRepository, container.orderRepository)
    }
    initializer { MyOrdersViewModel(container.authRepository, container.orderRepository) }
    initializer { OrderDetailViewModel(createSavedStateHandle(), container.orderRepository) }
    initializer { ProfileViewModel(container.authRepository) }
}
