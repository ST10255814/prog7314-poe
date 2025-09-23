package com.example.rentwise.retrofit_instance

import com.example.rentwise.faq.OpenRouterApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object OpenRouterInstance {
    private const val BASE_URL = "https://openrouter.ai/api/v1/"
    private const val API_KEY = "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" // AI API KEY

    fun createAPI(): OpenRouterApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $API_KEY")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("HTTP-Referer", "app://rentwise")
                    .addHeader("X-Title", "RentWise FAQ ChatBot")
                    .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenRouterApiService::class.java)
    }
}