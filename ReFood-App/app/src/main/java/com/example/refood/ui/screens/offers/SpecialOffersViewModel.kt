@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.refood.ui.screens.offers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.refood.data.repository.AuthRepository
import com.example.refood.data.repository.CartRepository
import com.example.refood.data.repository.ProductRepository
import com.example.refood.domain.model.Product
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class SpecialOffersUiState(
    val offers: List<Product> = emptyList(),
    val cartCount: Int = 0,
    val isLoading: Boolean = true
)

class SpecialOffersViewModel(
    authRepository: AuthRepository,
    productRepository: ProductRepository,
    cartRepository: CartRepository
) : ViewModel() {

    private val userIdFlow = authRepository.currentUserId.filterNotNull()
    private val cartCountFlow = userIdFlow.flatMapLatest { cartRepository.observeCartItemCount(it) }

    val uiState: StateFlow<SpecialOffersUiState> = combine(
        productRepository.observeOffers(),
        cartCountFlow
    ) { offers, cartCount ->
        SpecialOffersUiState(offers = offers, cartCount = cartCount, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SpecialOffersUiState())
}
