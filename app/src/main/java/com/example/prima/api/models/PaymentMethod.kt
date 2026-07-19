package com.example.prima.api.models

data class PaymentMethod(
    val id: Int,
    val name: String,
    val description: String?,
    val is_active: Boolean?
)

data class PaymentMethodResponse(
    val status: Int,
    val message: String,
    val data: List<PaymentMethod>? = null
)
