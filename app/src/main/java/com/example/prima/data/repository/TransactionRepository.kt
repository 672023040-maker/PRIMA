package com.example.prima.data.repository

import com.example.prima.api.RetrofitClient
import com.example.prima.api.models.*

class TransactionRepository {

    private val api = RetrofitClient.apiService

    suspend fun getProducts(token: String): Result<ProductResponse> {
        return try {
            val response = api.getProducts("Bearer $token")
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Gagal memuat produk"))
                }
            } else {
                Result.failure(Exception("Gagal memuat produk (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server: ${e.localizedMessage}"))
        }
    }

    suspend fun getCategories(token: String): Result<CategoryResponse> {
        return try {
            val response = api.getCategories("Bearer $token")
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Gagal memuat kategori"))
                }
            } else {
                Result.failure(Exception("Gagal memuat kategori (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server: ${e.localizedMessage}"))
        }
    }

    suspend fun getPaymentMethods(token: String): Result<PaymentMethodResponse> {
        return try {
            val response = api.getPaymentMethods("Bearer $token")
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Gagal memuat metode pembayaran"))
                }
            } else {
                Result.failure(Exception("Gagal memuat metode pembayaran (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server: ${e.localizedMessage}"))
        }
    }

    suspend fun createTransaction(token: String, kasirId: Int): Result<TransactionResponse> {
        return try {
            val response = api.createTransaction(
                "Bearer $token",
                TransactionRequest(kasirId)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Gagal membuat transaksi"))
                }
            } else {
                Result.failure(Exception("Gagal membuat transaksi (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server: ${e.localizedMessage}"))
        }
    }

    suspend fun addDetail(
        token: String,
        transactionId: Int,
        productId: Int,
        quantity: Int
    ): Result<TransactionDetailResponse> {
        return try {
            val response = api.addTransactionDetail(
                "Bearer $token",
                transactionId,
                TransactionDetailRequest(productId, quantity)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Gagal menambah detail"))
                }
            } else {
                Result.failure(Exception("Gagal menambah detail (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server: ${e.localizedMessage}"))
        }
    }

    suspend fun completeTransaction(
        token: String,
        transactionId: Int,
        paymentMethodId: Int,
        amountPaid: Double
    ): Result<CompleteTransactionResponse> {
        return try {
            val response = api.completeTransaction(
                "Bearer $token",
                transactionId,
                CompleteTransactionRequest(paymentMethodId, amountPaid)
            )
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Gagal menyelesaikan transaksi"))
                }
            } else {
                Result.failure(Exception("Gagal menyelesaikan transaksi (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server: ${e.localizedMessage}"))
        }
    }

    suspend fun getTransactions(token: String): Result<TransactionListResponse> {
        return try {
            val response = api.getTransactions("Bearer $token")
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Gagal memuat transaksi"))
                }
            } else {
                Result.failure(Exception("Gagal memuat transaksi (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Gagal terhubung ke server: ${e.localizedMessage}"))
        }
    }
}
