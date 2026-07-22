@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.refood.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.refood.data.repository.AuthRepository
import com.example.refood.data.repository.CartRepository
import com.example.refood.data.repository.ProductRepository
import com.example.refood.domain.model.Product
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val userName: String = "",
    val offers: List<Product> = emptyList(),
    val recentProducts: List<Product> = emptyList(),
    val cartCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class HomeViewModel(
    authRepository: AuthRepository,
    productRepository: ProductRepository,
    cartRepository: CartRepository
) : ViewModel() {

    private val userIdFlow = authRepository.currentUserId.filterNotNull()

    private val userNameFlow = userIdFlow.map { id -> authRepository.getUser(id)?.name.orEmpty() }

    val uiState: StateFlow<HomeUiState> = combine(
        userNameFlow,
        productRepository.observeOffers(),
        productRepository.observeAll(),
        userIdFlow.flatMapLatest { userId -> cartRepository.observeCartItemCount(userId) }
    ) { name, offers, all, cartCount ->
        HomeUiState(
            userName = name,
            offers = offers.take(6),
            recentProducts = all.take(8),
            cartCount = cartCount,
            isLoading = false
        )
    }.catch { emit(HomeUiState(isLoading = false, errorMessage = "No se pudo conectar con el servidor.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
}
