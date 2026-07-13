package com.example.refood.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.refood.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val phone: String = "",
    val address: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registerSuccess: Boolean = false
)

class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = update { it.copy(name = value) }
    fun onEmailChange(value: String) = update { it.copy(email = value) }
    fun onPasswordChange(value: String) = update { it.copy(password = value) }
    fun onConfirmPasswordChange(value: String) = update { it.copy(confirmPassword = value) }
    fun onPhoneChange(value: String) = update { it.copy(phone = value) }
    fun onAddressChange(value: String) = update { it.copy(address = value) }

    private fun update(block: (RegisterUiState) -> RegisterUiState) {
        _uiState.update { block(it).copy(errorMessage = null) }
    }

    fun register() {
        val state = _uiState.value
        val validationError = validate(state)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            authRepository.register(
                name = state.name,
                email = state.email,
                password = state.password,
                phone = state.phone,
                address = state.address
            ).onSuccess {
                _uiState.update { it.copy(isLoading = false, registerSuccess = true) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "No se pudo crear la cuenta.")
                }
            }
        }
    }

    private fun validate(state: RegisterUiState): String? {
        if (state.name.isBlank()) return "Ingresa tu nombre completo."
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            return "Ingresa un correo electrónico válido."
        }
        if (state.password.length < 6) return "La contraseña debe tener al menos 6 caracteres."
        if (state.password != state.confirmPassword) return "Las contraseñas no coinciden."
        if (state.phone.isBlank()) return "Ingresa tu número de teléfono."
        if (state.address.isBlank()) return "Ingresa tu dirección de entrega."
        return null
    }
}
