@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.refood.ui.screens.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.refood.data.repository.AuthRepository
import com.example.refood.data.repository.CartRepository
import com.example.refood.data.repository.OrderRepository
import com.example.refood.domain.model.CartLine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

val PAYMENT_METHODS = listOf("Efectivo", "Tarjeta", "Yape / Plin")

data class OrderFormUiState(
    val lines: List<CartLine> = emptyList(),
    val total: Double = 0.0,
    val deliveryAddress: String = "",
    val paymentMethod: String = PAYMENT_METHODS.first(),
    val notes: String = "",
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val placedOrderId: Long? = null
)

private data class FormFields(
    val address: String,
    val payment: String,
    val notes: String,
    val submitting: Boolean,
    val error: String?,
    val orderId: Long?
)

class OrderFormViewModel(
    private val authRepository: AuthRepository,
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val userIdFlow = authRepository.currentUserId.filterNotNull()
    private val deliveryAddress = MutableStateFlow("")
    private val paymentMethod = MutableStateFlow(PAYMENT_METHODS.first())
    private val notes = MutableStateFlow("")
    private val isSubmitting = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val placedOrderId = MutableStateFlow<Long?>(null)

    init {
        viewModelScope.launch {
            val userId = userIdFlow.first()
            val user = authRepository.getUser(userId)
            deliveryAddress.value = user?.address.orEmpty()
        }
    }

    private val formFieldsFlow = combine(
        combine(deliveryAddress, paymentMethod, notes) { a, p, n -> Triple(a, p, n) },
        isSubmitting,
        errorMessage,
        placedOrderId
    ) { (address, payment, n), submitting, error, orderId ->
        FormFields(address, payment, n, submitting, error, orderId)
    }

    val uiState: StateFlow<OrderFormUiState> = combine(
        userIdFlow.flatMapLatest { cartRepository.observeCart(it) },
        formFieldsFlow
    ) { lines, form ->
        OrderFormUiState(
            lines = lines,
            total = lines.sumOf { it.lineTotal },
            deliveryAddress = form.address,
            paymentMethod = form.payment,
            notes = form.notes,
            isLoading = false,
            isSubmitting = form.submitting,
            errorMessage = form.error,
            placedOrderId = form.orderId
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrderFormUiState())

    fun onAddressChange(value: String) {
        deliveryAddress.value = value
        errorMessage.value = null
    }

    fun onPaymentMethodChange(value: String) {
        paymentMethod.value = value
    }

    fun onNotesChange(value: String) {
        notes.value = value
    }

    fun submitOrder() {
        val state = uiState.value
        if (state.lines.isEmpty()) {
            errorMessage.value = "Tu carrito está vacío."
            return
        }
        if (state.deliveryAddress.isBlank()) {
            errorMessage.value = "Ingresa una dirección de entrega."
            return
        }
        viewModelScope.launch {
            isSubmitting.value = true
            val userId = userIdFlow.first()
            orderRepository.placeOrder(
                userId = userId,
                deliveryAddress = state.deliveryAddress,
                paymentMethod = state.paymentMethod,
                notes = state.notes,
                lines = state.lines
            ).onSuccess { orderId ->
                isSubmitting.value = false
                placedOrderId.value = orderId
            }.onFailure { error ->
                isSubmitting.value = false
                errorMessage.value = error.message ?: "No se pudo registrar el pedido."
            }
        }
    }
}
