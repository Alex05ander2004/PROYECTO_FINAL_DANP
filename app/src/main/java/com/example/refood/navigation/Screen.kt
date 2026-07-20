package com.example.refood.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Storefront
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

/** Destinos de nivel superior mostrados en la barra de navegación inferior.
 *  Ícono outline en reposo, filled cuando está seleccionado (patrón sutil de apps premium). */
enum class TopLevelDestination(
    val screen: Screen,
    val label: String,
    val iconOutlined: ImageVector,
    val iconFilled: ImageVector
) {
    HOME(Screen.Home, "Inicio", Icons.Outlined.Home, Icons.Filled.Home),
    PRODUCTS(Screen.Products, "Productos", Icons.Outlined.Storefront, Icons.Filled.Storefront),
    OFFERS(Screen.Offers, "Ofertas", Icons.Outlined.LocalOffer, Icons.Filled.LocalOffer),
    ORDERS(Screen.MyOrders, "Pedidos", Icons.Outlined.ReceiptLong, Icons.Filled.ReceiptLong),
    PROFILE(Screen.Profile, "Perfil", Icons.Outlined.Person, Icons.Filled.Person)
}
