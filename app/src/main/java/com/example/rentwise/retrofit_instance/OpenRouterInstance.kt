package com.example.rentwise.retrofit_instance

import android.util.Log
import com.example.rentwise.faq.OpenRouterApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Singleton object responsible for configuring and providing an instance of the OpenRouter API service.
// It sets up the Retrofit client with custom headers, logging, and timeout settings for secure and reliable communication with the OpenRouter AI API.
object OpenRouterInstance {
    // Base URL for all OpenRouter API requests, ensuring all endpoints are relative to this root.
    private const val BASE_URL = "https://openrouter.ai/api/v1/"
    // API key used for authenticating requests to the OpenRouter AI service.
    private const val API_KEY = "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"

    // Creates and returns an implementation of the OpenRouterApiService interface, configured with custom headers and logging.
    fun createAPI(): OpenRouterApiService {
        // Configures HTTP logging to capture request and response details for debugging.
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Builds an OkHttpClient with custom timeouts, logging, and required headers for every request.
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Sets connection timeout to 30 seconds.
            .readTimeout(30, TimeUnit.SECONDS)    // Sets read timeout to 30 seconds.
            .writeTimeout(30, TimeUnit.SECONDS)   // Sets write timeout to 30 seconds.
            .addInterceptor(logging)              // Adds logging interceptor for HTTP traffic.
            .addInterceptor { chain ->
                // Adds authentication and metadata headers to every outgoing request.
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $API_KEY") // API key for authentication.
                    .addHeader("Content-Type", "application/json") // Specifies JSON payloads.
                    .addHeader("HTTP-Referer", "app://rentwise")   // Identifies the app as the request source.
                    .addHeader("X-Title", "RentWise FAQ ChatBot")  // Custom title for API usage context.
                    .build()

                chain.proceed(request)
            }
            .build()

        // Constructs and returns a Retrofit instance configured for the OpenRouter API.
        return Retrofit.Builder()
            .baseUrl(BASE_URL)                        // Sets the API base URL.
            .client(client)                           // Attaches the custom OkHttpClient.
            .addConverterFactory(GsonConverterFactory.create()) // Enables JSON serialization/deserialization.
            .build()
            .create(OpenRouterApiService::class.java) // Creates the API service interface implementation.
    }
}
