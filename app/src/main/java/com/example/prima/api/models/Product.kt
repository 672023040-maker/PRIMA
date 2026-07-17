package com.example.prima.api.models

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val category: String?,
    val description: String?,
    val image_url: String?
)

data class ProductResponse(
    val status: Int,
    val message: String,
    val data: List<Product>? = null
)
