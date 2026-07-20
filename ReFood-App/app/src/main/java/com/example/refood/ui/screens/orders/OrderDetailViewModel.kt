package com.example.refood.ui.screens.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.refood.data.repository.OrderRepository
import com.example.refood.domain.model.Order
import com.example.refood.navigation.Screen
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class OrderDetailUiState(
    val order: Order? = null,
    val isLoading: Boolean = true
)

class OrderDetailViewModel(
    savedStateHandle: SavedStateHandle,
    orderRepository: OrderRepository
) : ViewModel() {

    private val orderId: Long =
        checkNotNull(savedStateHandle.get<String>(Screen.OrderDetail.ARG_ORDER_ID)).toLong()

    val uiState: StateFlow<OrderDetailUiState> = orderRepository.observeOrderDetail(orderId)
        .map { order -> OrderDetailUiState(order = order, isLoading = order == null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrderDetailUiState())
}
