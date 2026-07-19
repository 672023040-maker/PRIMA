package com.example.prima.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var baseUrl: String = "http://10.0.2.2:3000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private var retrofit: Retrofit = createRetrofit()

    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun setBaseUrl(url: String) {
        val normalizedUrl = url.trimEnd('/') + "/"
        if (normalizedUrl != baseUrl) {
            baseUrl = normalizedUrl
            retrofit = createRetrofit()
            apiService = retrofit.create(ApiService::class.java)
        }
    }

    var apiService: ApiService = retrofit.create(ApiService::class.java)
        private set
}
