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

    // Use BuildConfig to provide the API key at build time (do not hard-code secrets in source files for production).
    // Fully qualify BuildConfig to avoid unresolved import problems in environments where the generated BuildConfig is not present.
    private val API_KEY: String = com.example.rentwise.BuildConfig.OPENROUTER_API_KEY ?: ""

    // Creates and returns an implementation of the OpenRouterApiService interface, configured with custom headers and logging.
    fun createAPI(): OpenRouterApiService {
        // Configures HTTP logging to capture request and response details for debugging.
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Validate API key early and log a clear message if missing
        if (API_KEY.isBlank()) {
            Log.e("OpenRouterInstance", "OpenRouter API key is EMPTY. Set OPENROUTER_API_KEY in app/build.gradle or gradle.properties and rebuild.")
        }

        // Builds an OkHttpClient with custom timeouts, logging, and required headers for every request.
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS) // Sets connection timeout to 30 seconds.
            .readTimeout(30, TimeUnit.SECONDS)    // Sets read timeout to 30 seconds.
            .writeTimeout(30, TimeUnit.SECONDS)   // Sets write timeout to 30 seconds.
            .addInterceptor(logging)              // Adds logging interceptor for HTTP traffic.
            .addInterceptor { chain ->
                // Adds authentication and metadata headers to every outgoing request.
                val builder = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json") // Specifies JSON payloads.
                    .addHeader("Referer", "app://rentwise")   // Identifies the app as the request source.
                    .addHeader("X-Title", "RentWise FAQ ChatBot")  // Custom title for API usage context.

                // Only add Authorization header if API_KEY is present
                if (API_KEY.isNotBlank()) {
                    builder.addHeader("Authorization", "Bearer $API_KEY")
                }

                val request = builder.build()

                // Debug: log whether Authorization header is present (only in debug builds)
                if (com.example.rentwise.BuildConfig.DEBUG) {
                    try {
                        val authHeader = request.header("Authorization")
                        if (authHeader.isNullOrEmpty()) {
                            Log.d("OpenRouterInstance", "Authorization header is MISSING on outgoing request")
                        } else {
                            // Avoid printing full secret in logs; show masked form
                            val masked = if (authHeader.length > 10) authHeader.take(10) + "...[masked]" else authHeader
                            Log.d("OpenRouterInstance", "Outgoing Authorization header: $masked")
                        }
                    } catch (e: Exception) {
                        Log.w("OpenRouterInstance", "Failed to read Authorization header for debug logging: ${e.message}")
                    }
                }

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
