package com.example.prima.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prima.api.models.Product
import com.example.prima.data.SessionManager
import com.example.prima.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val errorMessage: String? = null
)

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TransactionRepository()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    fun loadProducts() {
        val token = sessionManager.getToken()
        if (token == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Sesi tidak ditemukan, silakan login ulang"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = repository.getProducts(token)
            result.onSuccess { response ->
                _uiState.value = ProductUiState(
                    products = response.data ?: emptyList()
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Gagal memuat produk"
                )
            }
        }
    }
}
