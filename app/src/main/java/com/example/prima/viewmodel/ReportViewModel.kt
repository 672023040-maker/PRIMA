package com.example.prima.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prima.api.models.Transaction
import com.example.prima.data.SessionManager
import com.example.prima.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportUiState(
    val isLoading: Boolean = false,
    val transactions: List<Transaction> = emptyList(),
    val totalRevenue: Double = 0.0,
    val errorMessage: String? = null
)

class ReportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TransactionRepository()
    private val sessionManager = SessionManager(application)

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun loadReports() {
        val token = sessionManager.getToken()
        if (token == null) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Sesi tidak ditemukan"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = repository.getTransactions(token)
            result.onSuccess { response ->
                val transactionList = response.data ?: emptyList()
                val total = transactionList.sumOf { it.total }
                _uiState.value = ReportUiState(
                    transactions = transactionList,
                    totalRevenue = total
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error.message ?: "Gagal memuat laporan"
                )
            }
        }
    }
}
