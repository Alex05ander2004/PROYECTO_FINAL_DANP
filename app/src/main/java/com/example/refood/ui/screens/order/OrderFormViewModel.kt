@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.refood.ui.screens.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.refood.data.repository.AuthRepository
import com.example.refood.data.repository.CartRepository
import com.example.refood.data.repository.OrderRepository
import com.example.refood.domain.model.CartLine
import com.example.refood.domain.validation.FieldValidators
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Debe coincidir con Order.PaymentMethod del backend (EFECTIVO / YAPE / PLIN).
val PAYMENT_METHODS = listOf("Efectivo", "Yape", "Plin")

/** Número de contacto de ReFood para pagos por Yape/Plin. Placeholder hasta que exista
 *  un dato real de la tienda/administrador en el backend. */
const val STORE_PAYMENT_NUMBER = "987 654 321"

fun paymentMethodRequiresReference(paymentMethod: String) = paymentMethod != "Efectivo"

data class OrderFormUiState(
    val lines: List<CartLine> = emptyList(),
    val total: Double = 0.0,
    val deliveryAddress: String = "",
    val paymentMethod: String = PAYMENT_METHODS.first(),
    val operationNumber: String = "",
    val notes: String = "",
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val placedOrderId: Long? = null
) {
    val requiresOperationNumber: Boolean get() = paymentMethodRequiresReference(paymentMethod)
}

private data class DraftFields(
    val address: String,
    val payment: String,
    val operationNumber: String,
    val notes: String
)

private data class FormFields(
    val draft: DraftFields,
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
    private val operationNumber = MutableStateFlow("")
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

    private val draftFieldsFlow = combine(
        deliveryAddress, paymentMethod, operationNumber, notes
    ) { address, payment, opNumber, n -> DraftFields(address, payment, opNumber, n) }

    private val formFieldsFlow = combine(
        draftFieldsFlow, isSubmitting, errorMessage, placedOrderId
    ) { draft, submitting, error, orderId -> FormFields(draft, submitting, error, orderId) }

    val uiState: StateFlow<OrderFormUiState> = combine(
        userIdFlow.flatMapLatest { cartRepository.observeCart(it) },
        formFieldsFlow
    ) { lines, form ->
        OrderFormUiState(
            lines = lines,
            total = lines.sumOf { it.lineTotal },
            deliveryAddress = form.draft.address,
            paymentMethod = form.draft.payment,
            operationNumber = form.draft.operationNumber,
            notes = form.draft.notes,
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
        errorMessage.value = null
    }

    fun onOperationNumberChange(value: String) {
        operationNumber.value = value.filter { it.isDigit() }.take(10)
        errorMessage.value = null
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
        val addressError = FieldValidators.addressError(state.deliveryAddress)
        if (addressError != null) {
            errorMessage.value = addressError
            return
        }
        if (state.requiresOperationNumber) {
            val operationError = FieldValidators.operationNumberError(state.operationNumber)
            if (operationError != null) {
                errorMessage.value = operationError
                return
            }
        }
        viewModelScope.launch {
            isSubmitting.value = true
            val userId = userIdFlow.first()
            orderRepository.placeOrder(
                userId = userId,
                deliveryAddress = state.deliveryAddress,
                paymentMethod = state.paymentMethod,
                notes = state.notes,
                lines = state.lines,
                paymentReference = if (state.requiresOperationNumber) state.operationNumber else null
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
