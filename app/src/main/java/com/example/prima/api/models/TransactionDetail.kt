package com.example.prima.api.models

data class TransactionDetailRequest(
    val product_id: Int,
    val quantity: Int
)

data class TransactionDetailResponse(
    val status: Int,
    val message: String,
    val data: TransactionDetail?
)

data class TransactionDetail(
    val id: Int,
    val transaction_id: Int,
    val product_id: Int,
    val product_name: String?,
    val quantity: Int,
    val price: Double,
    val subtotal: Double
)
