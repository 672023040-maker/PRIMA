package com.example.prima.api.models

data class CreateProductRequest(
    val name: String,
    val price: Double,
    val category: String,
    val description: String
)

data class UpdateProductRequest(
    val name: String,
    val price: Double,
    val category: String,
    val description: String
)

data class ProductDetailResponse(
    val status: Int,
    val message: String,
    val data: Product? = null
)
