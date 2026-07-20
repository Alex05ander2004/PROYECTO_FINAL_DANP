package com.example.refood.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.refood.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface SplashDestination {
    data object Loading : SplashDestination
    data object Home : SplashDestination
    data object Login : SplashDestination
}

class SplashViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.Loading)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            val userId = authRepository.currentUserId.first()
            _destination.value = if (userId != null) SplashDestination.Home else SplashDestination.Login
        }
    }
}
