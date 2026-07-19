package com.example.prima.data.repository

import com.example.prima.api.ApiService
import com.example.prima.api.RetrofitClient
import com.example.prima.api.models.ApiResponse
import com.example.prima.api.models.LoginRequest
import com.example.prima.api.models.LoginResponse

class AuthRepository(private val api: ApiService = RetrofitClient.apiService) {

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.status in 200..299) {
                    Result.success(body)
                } else {
                    Result.failure(Exception(body?.message ?: "Login failed"))
                }
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Kredensial tidak sesuai"
                    422 -> "Data tidak valid"
                    else -> "Login gagal (${response.code()})"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server: ${e.localizedMessage}"))
        }
    }

    suspend fun logout(token: String): Result<ApiResponse> {
        return try {
            val response = api.logout("Bearer $token")
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Logout failed"))
                }
            } else {
                Result.failure(Exception("Logout gagal (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server"))
        }
    }
}
