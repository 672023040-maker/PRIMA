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

    @POST("api/products")
    suspend fun createProduct(
        @Header("Authorization") token: String,
        @Body request: CreateProductRequest
    ): Response<ProductDetailResponse>

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Header("Authorization") token: String,
        @Path("id") id: Int,
        @Body request: UpdateProductRequest
    ): Response<ProductDetailResponse>

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ApiResponse>

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

    @GET("api/transactions")
    suspend fun getTransactions(
        @Header("Authorization") token: String
    ): Response<TransactionListResponse>
}
