package com.example.prima.api.models

data class Category(
    val id: Int,
    val name: String,
    val description: String?
)

data class CategoryResponse(
    val status: Int,
    val message: String,
    val data: List<Category>? = null
)
