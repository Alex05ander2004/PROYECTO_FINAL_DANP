package com.example.refood.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.refood.di.AppContainer
import com.example.refood.di.appViewModelFactory
import com.example.refood.ui.screens.auth.LoginScreen
import com.example.refood.ui.screens.auth.LoginViewModel
import com.example.refood.ui.screens.auth.RegisterScreen
import com.example.refood.ui.screens.auth.RegisterViewModel
import com.example.refood.ui.screens.auth.SplashScreen
import com.example.refood.ui.screens.auth.SplashViewModel
import com.example.refood.ui.screens.cart.CartScreen
import com.example.refood.ui.screens.cart.CartViewModel
import com.example.refood.ui.screens.home.HomeScreen
import com.example.refood.ui.screens.home.HomeViewModel
import com.example.refood.ui.screens.offers.SpecialOffersScreen
import com.example.refood.ui.screens.offers.SpecialOffersViewModel
import com.example.refood.ui.screens.order.OrderConfirmationScreen
import com.example.refood.ui.screens.order.OrderFormScreen
import com.example.refood.ui.screens.order.OrderFormViewModel
import com.example.refood.ui.screens.orders.MyOrdersScreen
import com.example.refood.ui.screens.orders.MyOrdersViewModel
import com.example.refood.ui.screens.orders.OrderDetailScreen
import com.example.refood.ui.screens.orders.OrderDetailViewModel
import com.example.refood.ui.screens.products.ProductDetailScreen
import com.example.refood.ui.screens.products.ProductDetailViewModel
import com.example.refood.ui.screens.products.ProductsScreen
import com.example.refood.ui.screens.products.ProductsViewModel
import com.example.refood.ui.screens.profile.ProfileScreen
import com.example.refood.ui.screens.profile.ProfileViewModel

@Composable
fun ReFoodNavGraph(container: AppContainer) {
    val navController = rememberNavController()
    val factory = appViewModelFactory(container)

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = TopLevelDestination.entries.any { it.screen.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.screen.route,
                            onClick = {
                                navController.navigate(destination.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                val viewModel: SplashViewModel = viewModel(factory = factory)
                SplashScreen(
                    viewModel = viewModel,
                    onNavigateHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Login.route) {
                val viewModel: LoginViewModel = viewModel(factory = factory)
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }

            composable(Screen.Register.route) {
                val viewModel: RegisterViewModel = viewModel(factory = factory)
                RegisterScreen(
                    viewModel = viewModel,
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Home.route) {
                val viewModel: HomeViewModel = viewModel(factory = factory)
                HomeScreen(
                    viewModel = viewModel,
                    onProductClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                    onSeeAllOffers = { navController.navigate(Screen.Offers.route) },
                    onSeeAllProducts = { navController.navigate(Screen.Products.route) },
                    onCartClick = { navController.navigate(Screen.Cart.route) }
                )
            }

            composable(Screen.Products.route) {
                val viewModel: ProductsViewModel = viewModel(factory = factory)
                ProductsScreen(
                    viewModel = viewModel,
                    onProductClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                    onCartClick = { navController.navigate(Screen.Cart.route) }
                )
            }

            composable(Screen.Offers.route) {
                val viewModel: SpecialOffersViewModel = viewModel(factory = factory)
                SpecialOffersScreen(
                    viewModel = viewModel,
                    onProductClick = { id -> navController.navigate(Screen.ProductDetail.createRoute(id)) },
                    onCartClick = { navController.navigate(Screen.Cart.route) }
                )
            }

            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(navArgument(Screen.ProductDetail.ARG_PRODUCT_ID) { type = NavType.StringType })
            ) {
                val viewModel: ProductDetailViewModel = viewModel(factory = factory)
                ProductDetailScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onCartClick = { navController.navigate(Screen.Cart.route) }
                )
            }

            composable(Screen.Cart.route) {
                val viewModel: CartViewModel = viewModel(factory = factory)
                CartScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onCheckout = { navController.navigate(Screen.OrderForm.route) }
                )
            }

            composable(Screen.OrderForm.route) {
                val viewModel: OrderFormViewModel = viewModel(factory = factory)
                OrderFormScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOrderPlaced = { orderId ->
                        navController.navigate(Screen.OrderConfirmation.createRoute(orderId)) {
                            popUpTo(Screen.Home.route)
                        }
                    }
                )
            }

            composable(
                route = Screen.OrderConfirmation.route,
                arguments = listOf(navArgument(Screen.OrderConfirmation.ARG_ORDER_ID) { type = NavType.LongType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getLong(Screen.OrderConfirmation.ARG_ORDER_ID) ?: 0L
                OrderConfirmationScreen(
                    orderId = orderId,
                    onSeeOrders = {
                        navController.navigate(Screen.MyOrders.route) {
                            popUpTo(Screen.Home.route)
                        }
                    },
                    onContinueShopping = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.MyOrders.route) {
                val viewModel: MyOrdersViewModel = viewModel(factory = factory)
                MyOrdersScreen(
                    viewModel = viewModel,
                    onOrderClick = { id -> navController.navigate(Screen.OrderDetail.createRoute(id)) }
                )
            }

            composable(
                route = Screen.OrderDetail.route,
                arguments = listOf(navArgument(Screen.OrderDetail.ARG_ORDER_ID) { type = NavType.StringType })
            ) {
                val viewModel: OrderDetailViewModel = viewModel(factory = factory)
                OrderDetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }

            composable(Screen.Profile.route) {
                val viewModel: ProfileViewModel = viewModel(factory = factory)
                ProfileScreen(
                    viewModel = viewModel,
                    onLoggedOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0)
                        }
                    }
                )
            }
        }
    }
}
