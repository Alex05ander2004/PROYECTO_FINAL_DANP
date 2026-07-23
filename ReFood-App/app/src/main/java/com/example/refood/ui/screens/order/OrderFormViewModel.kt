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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Debe coincidir con Order.PaymentMethod del backend (TARJETA / YAPE / PLIN).
val PAYMENT_METHODS = listOf("Tarjeta", "Yape", "Plin")

/** Número de contacto de ReFood para pagos por Yape/Plin. Placeholder hasta que exista
 *  un dato real de la tienda/administrador en el backend. */
const val STORE_PAYMENT_NUMBER = "987 654 321"

fun isCardPayment(paymentMethod: String) = paymentMethod == "Tarjeta"

data class OrderFormUiState(
    val lines: List<CartLine> = emptyList(),
    val total: Double = 0.0,
    val deliveryAddress: String = "",
    val paymentMethod: String = PAYMENT_METHODS.first(),
    val operationNumber: String = "",
    val cardNumber: String = "",
    val cardExpiry: String = "",
    val cardCvv: String = "",
    val cardHolderName: String = "",
    val notes: String = "",
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val placedOrderId: Long? = null
) {
    val isCardSelected: Boolean get() = isCardPayment(paymentMethod)
}

private data class PaymentDetails(
    val operationNumber: String,
    val cardNumber: String,
    val cardExpiry: String,
    val cardCvv: String,
    val cardHolderName: String
)

private data class DraftFields(
    val address: String,
    val payment: String,
    val notes: String,
    val details: PaymentDetails
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
    private val cardNumber = MutableStateFlow("")
    private val cardExpiry = MutableStateFlow("")
    private val cardCvv = MutableStateFlow("")
    private val cardHolderName = MutableStateFlow("")
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

    private val paymentDetailsFlow = combine(
        operationNumber, cardNumber, cardExpiry, cardCvv, cardHolderName
    ) { op, number, expiry, cvv, holder -> PaymentDetails(op, number, expiry, cvv, holder) }

    private val draftFieldsFlow = combine(
        deliveryAddress, paymentMethod, notes, paymentDetailsFlow
    ) { address, payment, n, details -> DraftFields(address, payment, n, details) }

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
            operationNumber = form.draft.details.operationNumber,
            cardNumber = form.draft.details.cardNumber,
            cardExpiry = form.draft.details.cardExpiry,
            cardCvv = form.draft.details.cardCvv,
            cardHolderName = form.draft.details.cardHolderName,
            notes = form.draft.notes,
            isLoading = false,
            isSubmitting = form.submitting,
            errorMessage = form.error,
            placedOrderId = form.orderId
        )
    }.catch { emit(OrderFormUiState(isLoading = false, errorMessage = "No se pudo conectar con el servidor.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OrderFormUiState())

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

    fun onCardNumberChange(value: String) {
        cardNumber.value = value.filter { it.isDigit() }.take(19)
        errorMessage.value = null
    }

    fun onCardExpiryChange(value: String) {
        val digits = value.filter { it.isDigit() }.take(4)
        cardExpiry.value = if (digits.length > 2) "${digits.take(2)}/${digits.drop(2)}" else digits
        errorMessage.value = null
    }

    fun onCardCvvChange(value: String) {
        cardCvv.value = value.filter { it.isDigit() }.take(4)
        errorMessage.value = null
    }

    fun onCardHolderNameChange(value: String) {
        cardHolderName.value = value.filter { !it.isDigit() }
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

        val paymentReference: String
        if (state.isCardSelected) {
            val cardNumberError = FieldValidators.cardNumberError(state.cardNumber)
            if (cardNumberError != null) {
                errorMessage.value = cardNumberError
                return
            }
            val expiryError = FieldValidators.cardExpiryError(state.cardExpiry)
            if (expiryError != null) {
                errorMessage.value = expiryError
                return
            }
            val cvvError = FieldValidators.cardCvvError(state.cardCvv)
            if (cvvError != null) {
                errorMessage.value = cvvError
                return
            }
            val holderError = FieldValidators.cardHolderNameError(state.cardHolderName)
            if (holderError != null) {
                errorMessage.value = holderError
                return
            }
            // Nunca se envia el numero completo ni el CVV: solo los ultimos 4
            // digitos, igual que cualquier confirmacion de pago real.
            paymentReference = state.cardNumber.takeLast(4)
        } else {
            val operationError = FieldValidators.operationNumberError(state.operationNumber)
            if (operationError != null) {
                errorMessage.value = operationError
                return
            }
            paymentReference = state.operationNumber
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
                paymentReference = paymentReference
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
