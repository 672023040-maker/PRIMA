package com.example.prima.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prima.api.models.CreateProductRequest
import com.example.prima.api.models.Product
import com.example.prima.api.models.UpdateProductRequest
import com.example.prima.data.SessionManager
import com.example.prima.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isSaving: Boolean = false
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

    fun createProduct(name: String, price: Double, category: String, description: String) {
        val token = sessionManager.getToken()
        if (token == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Sesi tidak ditemukan")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            val result = repository.createProduct(token, CreateProductRequest(name, price, category, description))
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    successMessage = "Produk berhasil ditambahkan"
                )
                loadProducts()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = error.message ?: "Gagal menambahkan produk"
                )
            }
        }
    }

    fun updateProduct(id: Int, name: String, price: Double, category: String, description: String) {
        val token = sessionManager.getToken()
        if (token == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Sesi tidak ditemukan")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            val result = repository.updateProduct(token, id, UpdateProductRequest(name, price, category, description))
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    successMessage = "Produk berhasil diperbarui"
                )
                loadProducts()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = error.message ?: "Gagal memperbarui produk"
                )
            }
        }
    }

    fun deleteProduct(id: Int) {
        val token = sessionManager.getToken()
        if (token == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Sesi tidak ditemukan")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            val result = repository.deleteProduct(token, id)
            result.onSuccess {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    successMessage = "Produk berhasil dihapus"
                )
                loadProducts()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = error.message ?: "Gagal menghapus produk"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun getUserRole(): String? = sessionManager.getUserRole()
}
