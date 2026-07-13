package com.example.refood.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")

    data object Home : Screen("home")
    data object Products : Screen("products")
    data object Offers : Screen("offers")
    data object Cart : Screen("cart")
    data object MyOrders : Screen("orders")
    data object Profile : Screen("profile")

    data object ProductDetail : Screen("product/{productId}") {
        fun createRoute(productId: Long) = "product/$productId"
        const val ARG_PRODUCT_ID = "productId"
    }

    data object OrderForm : Screen("order-form")

    data object OrderConfirmation : Screen("order-confirmation/{orderId}") {
        fun createRoute(orderId: Long) = "order-confirmation/$orderId"
        const val ARG_ORDER_ID = "orderId"
    }

    data object OrderDetail : Screen("order/{orderId}") {
        fun createRoute(orderId: Long) = "order/$orderId"
        const val ARG_ORDER_ID = "orderId"
    }
}

/** Destinos de nivel superior mostrados en la barra de navegación inferior. */
enum class TopLevelDestination(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
) {
    HOME(Screen.Home, "Inicio", Icons.Filled.Home),
    PRODUCTS(Screen.Products, "Productos", Icons.Filled.Storefront),
    OFFERS(Screen.Offers, "Ofertas", Icons.Filled.LocalOffer),
    ORDERS(Screen.MyOrders, "Pedidos", Icons.Filled.ReceiptLong),
    PROFILE(Screen.Profile, "Perfil", Icons.Filled.Person)
}
