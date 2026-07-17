package com.example.prima.api.models

data class LoginResponse(
    val status: Int,
    val message: String,
    val token: String?,
    val user: UserInfo?
)

data class UserInfo(
    val id: Int,
    val name: String,
    val email: String,
    val role: String
)
