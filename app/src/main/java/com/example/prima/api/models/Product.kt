package com.example.prima.api.models

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val category_id: Int?,
    val category_name: String?,
    val description: String?,
    val image_url: String?,
    val stock: Int?,
    val is_active: Boolean?
)

data class ProductResponse(
    val status: Int,
    val message: String,
    val data: List<Product>? = null
)
