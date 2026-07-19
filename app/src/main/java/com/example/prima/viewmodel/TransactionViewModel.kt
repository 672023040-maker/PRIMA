package com.example.prima.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prima.api.models.Product
import com.example.prima.api.models.Transaction
import com.example.prima.api.models.PaymentMethod
import com.example.prima.data.SessionManager
import com.example.prima.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class CartItem(
    val product: Product,
    val quantity: Int
)

data class TransactionUiState(
    val isLoading: Boolean = false,
    val isCreatingTransaction: Boolean = false,
    val isSubmitting: Boolean = false,
    val currentTransaction: Transaction? = null,
    val lastCompletedTransaction: Transaction? = null,
    val cartItems: List<CartItem> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val selectedPaymentMethod: PaymentMethod? = null,
    val amountPaid: String = "",
    val showPaymentDialog: Boolean = false,
    val showReceiptScreen: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TransactionRepository()
    private val sessionManager = SessionManager(application)
    private val transactionMutex = Mutex()

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    init {
        loadPaymentMethods()
    }

    private fun loadPaymentMethods() {
        val token = sessionManager.getToken() ?: return
        viewModelScope.launch {
            val result = repository.getPaymentMethods(token)
            result.onSuccess { response ->
                _uiState.value = _uiState.value.copy(
                    paymentMethods = response.data ?: emptyList()
                )
            }
        }
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _uiState.value = _uiState.value.copy(selectedPaymentMethod = method)
    }

    fun updateAmountPaid(amount: String) {
        _uiState.value = _uiState.value.copy(amountPaid = amount)
    }

    fun createTransaction() {
        if (_uiState.value.isCreatingTransaction) return

        val token = sessionManager.getToken()
        if (token == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Sesi tidak ditemukan, silakan login ulang"
            )
            return
        }

        val kasirId = sessionManager.getUserId()
        if (kasirId <= 0) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Data kasir tidak valid"
            )
            return
        }

        viewModelScope.launch {
            transactionMutex.withLock {
                if (_uiState.value.isCreatingTransaction) return@withLock
                if (_uiState.value.currentTransaction != null) return@withLock

                _uiState.value = _uiState.value.copy(
                    isCreatingTransaction = true,
                    errorMessage = null
                )

                val result = repository.createTransaction(token, kasirId)
                result.onSuccess { response ->
                    val transaction = response.data
                    if (transaction != null) {
                        _uiState.value = _uiState.value.copy(
                            isCreatingTransaction = false,
                            currentTransaction = transaction,
                            successMessage = "Transaksi ${transaction.transaction_code} berhasil dibuat"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isCreatingTransaction = false,
                            errorMessage = "Gagal membuat transaksi: respons kosong"
                        )
                    }
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isCreatingTransaction = false,
                        errorMessage = error.message ?: "Gagal membuat transaksi"
                    )
                }
            }
        }
    }

    fun addToCart(product: Product) {
        val currentItems = _uiState.value.cartItems.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.product.id == product.id }

        if (existingIndex >= 0) {
            val existing = currentItems[existingIndex]
            currentItems[existingIndex] = existing.copy(quantity = existing.quantity + 1)
        } else {
            currentItems.add(CartItem(product, 1))
        }

        _uiState.value = _uiState.value.copy(cartItems = currentItems)

        viewModelScope.launch {
            transactionMutex.withLock {
                val state = _uiState.value
                if (state.currentTransaction == null && !state.isCreatingTransaction) {
                    createTransaction()
                }
            }
        }
    }

    fun removeFromCart(productId: Int) {
        val currentItems = _uiState.value.cartItems.toMutableList()
        currentItems.removeAll { it.product.id == productId }
        _uiState.value = _uiState.value.copy(cartItems = currentItems)
    }

    fun updateQuantity(productId: Int, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }
        val currentItems = _uiState.value.cartItems.toMutableList()
        val index = currentItems.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            currentItems[index] = currentItems[index].copy(quantity = quantity)
            _uiState.value = _uiState.value.copy(cartItems = currentItems)
        }
    }

    fun submitOrder() {
        val state = _uiState.value

        if (state.isSubmitting) return

        val transactionId = state.currentTransaction?.id
        if (transactionId == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Belum ada transaksi aktif. Tambahkan produk terlebih dahulu."
            )
            return
        }

        val token = sessionManager.getToken()
        if (token == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Sesi tidak ditemukan"
            )
            return
        }

        val items = state.cartItems
        if (items.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Keranjang masih kosong"
            )
            return
        }

        viewModelScope.launch {
            transactionMutex.withLock {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = true,
                    errorMessage = null
                )

                var allSuccess = true
                val failedItems = mutableListOf<String>()

                for (item in items) {
                    val result = repository.addDetail(
                        token, transactionId, item.product.id, item.quantity
                    )
                    result.onFailure { error ->
                        allSuccess = false
                        failedItems.add("${item.product.name}: ${error.message}")
                    }
                }

                if (allSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        showPaymentDialog = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        errorMessage = "Gagal memproses: ${failedItems.joinToString("; ")}"
                    )
                }
            }
        }
    }

    fun completePayment() {
        val state = _uiState.value
        val transactionId = state.currentTransaction?.id
        val paymentMethod = state.selectedPaymentMethod
        val amountPaidStr = state.amountPaid

        if (transactionId == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Tidak ada transaksi aktif")
            return
        }

        if (paymentMethod == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Pilih metode pembayaran")
            return
        }

        val amountPaid = amountPaidStr.toDoubleOrNull()
        if (amountPaid == null || amountPaid <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Masukkan jumlah bayar yang valid")
            return
        }

        val token = sessionManager.getToken() ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)

            val result = repository.completeTransaction(token, transactionId, paymentMethod.id, amountPaid)
            result.onSuccess { response ->
                _uiState.value = TransactionUiState(
                    lastCompletedTransaction = response.data,
                    showReceiptScreen = true,
                    paymentMethods = state.paymentMethods
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = error.message ?: "Gagal memproses pembayaran"
                )
            }
        }
    }

    fun getTotal(): Double {
        return _uiState.value.cartItems.sumOf { it.product.price * it.quantity }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun dismissPaymentDialog() {
        _uiState.value = _uiState.value.copy(showPaymentDialog = false)
    }

    fun dismissReceiptScreen() {
        _uiState.value = _uiState.value.copy(
            showReceiptScreen = false,
            lastCompletedTransaction = null
        )
    }
}
