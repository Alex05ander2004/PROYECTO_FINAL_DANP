@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.refood.ui.screens.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.refood.data.repository.AuthRepository
import com.example.refood.data.repository.CartRepository
import com.example.refood.data.repository.ProductRepository
import com.example.refood.domain.model.Product
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class ProductsUiState(
    val query: String = "",
    val selectedCategory: String? = null,
    val categories: List<String> = emptyList(),
    val products: List<Product> = emptyList(),
    val cartCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class ProductsViewModel(
    authRepository: AuthRepository,
    productRepository: ProductRepository,
    cartRepository: CartRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedCategory = MutableStateFlow<String?>(null)
    private val userIdFlow = authRepository.currentUserId.filterNotNull()
    private val cartCountFlow = userIdFlow.flatMapLatest { cartRepository.observeCartItemCount(it) }

    val uiState: StateFlow<ProductsUiState> = combine(
        query,
        selectedCategory,
        productRepository.observeAll(),
        productRepository.observeCategories(),
        cartCountFlow
    ) { q, category, products, categories, cartCount ->
        val filtered = products.filter { product ->
            (category == null || product.category == category) &&
                (q.isBlank() || product.name.contains(q, ignoreCase = true))
        }
        ProductsUiState(
            query = q,
            selectedCategory = category,
            categories = categories,
            products = filtered,
            cartCount = cartCount,
            isLoading = false
        )
    }.catch { emit(ProductsUiState(isLoading = false, errorMessage = "No se pudo conectar con el servidor.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductsUiState())

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onCategorySelected(category: String?) {
        selectedCategory.value = category
    }
}
