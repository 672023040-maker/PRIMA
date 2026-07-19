package com.example.prima.api

import com.example.prima.api.models.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/logout")
    suspend fun logout(@Header("Authorization") token: String): Response<ApiResponse>

    @GET("api/products")
    suspend fun getProducts(@Header("Authorization") token: String): Response<ProductResponse>

    @GET("api/categories")
    suspend fun getCategories(@Header("Authorization") token: String): Response<CategoryResponse>

    @GET("api/payment-methods")
    suspend fun getPaymentMethods(@Header("Authorization") token: String): Response<PaymentMethodResponse>

    @POST("api/transactions")
    suspend fun createTransaction(
        @Header("Authorization") token: String,
        @Body request: TransactionRequest
    ): Response<TransactionResponse>

    @POST("api/transactions/{id}/details")
    suspend fun addTransactionDetail(
        @Header("Authorization") token: String,
        @Path("id") transactionId: Int,
        @Body request: TransactionDetailRequest
    ): Response<TransactionDetailResponse>

    @POST("api/transactions/{id}/complete")
    suspend fun completeTransaction(
        @Header("Authorization") token: String,
        @Path("id") transactionId: Int,
        @Body request: CompleteTransactionRequest
    ): Response<CompleteTransactionResponse>

    @GET("api/transactions")
    suspend fun getTransactions(
        @Header("Authorization") token: String
    ): Response<TransactionListResponse>
}
