package com.example.prima.api.models

data class TransactionRequest(
    val kasir_id: Int
)

data class TransactionResponse(
    val status: Int,
    val message: String,
    val data: Transaction? = null
)

data class Transaction(
    val id: Int,
    val transaction_code: String,
    val kasir_id: Int,
    val kasir_name: String?,
    val payment_method_id: Int?,
    val payment_method_name: String?,
    val total: Double,
    val amount_paid: Double?,
    val change_amount: Double?,
    val notes: String?,
    val status: String,
    val created_at: String,
    val details: List<TransactionDetail>? = null
)

data class TransactionListResponse(
    val status: Int,
    val message: String,
    val data: List<Transaction>? = null
)

data class CompleteTransactionRequest(
    val payment_method_id: Int,
    val amount_paid: Double
)

data class CompleteTransactionResponse(
    val status: Int,
    val message: String,
    val data: Transaction? = null
)
