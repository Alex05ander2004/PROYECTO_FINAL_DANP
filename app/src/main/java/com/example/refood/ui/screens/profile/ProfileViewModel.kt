package com.example.refood.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.refood.data.repository.AuthRepository
import com.example.refood.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val email: String = "",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saveMessage: String? = null,
    val loggedOut: Boolean = false
)

class ProfileViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var userId: Long? = null

    init {
        viewModelScope.launch {
            val id = authRepository.currentUserId.first() ?: return@launch
            userId = id
            val user = authRepository.getUser(id) ?: return@launch
            _uiState.update {
                it.copy(
                    email = user.email,
                    name = user.name,
                    phone = user.phone,
                    address = user.address,
                    isLoading = false
                )
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, saveMessage = null) }
    fun onPhoneChange(value: String) = _uiState.update { it.copy(phone = value, saveMessage = null) }
    fun onAddressChange(value: String) = _uiState.update { it.copy(address = value, saveMessage = null) }

    fun saveProfile() {
        val id = userId ?: return
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            authRepository.updateUser(
                User(id = id, name = state.name, email = state.email, phone = state.phone, address = state.address)
            ).onSuccess {
                _uiState.update { it.copy(isSaving = false, saveMessage = "Datos actualizados correctamente.") }
            }.onFailure { error ->
                _uiState.update { it.copy(isSaving = false, saveMessage = error.message ?: "No se pudo guardar.") }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(loggedOut = true) }
        }
    }
}
