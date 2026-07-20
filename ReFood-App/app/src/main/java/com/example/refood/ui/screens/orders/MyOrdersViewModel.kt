@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.refood.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.refood.data.repository.AuthRepository
import com.example.refood.data.repository.OrderRepository
import com.example.refood.domain.model.Order
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class MyOrdersUiState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = true
)

class MyOrdersViewModel(
    authRepository: AuthRepository,
    orderRepository: OrderRepository
) : ViewModel() {

    val uiState: StateFlow<MyOrdersUiState> = authRepository.currentUserId
        .filterNotNull()
        .flatMapLatest { userId -> orderRepository.observeOrdersForUser(userId) }
        .map { orders -> MyOrdersUiState(orders = orders, isLoading = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MyOrdersUiState())
}
