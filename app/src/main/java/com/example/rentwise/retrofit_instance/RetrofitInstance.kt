package com.example.rentwise.retrofit_instance

import android.content.Context
import com.example.rentwise.api_interface.RentWiseApi
import com.example.rentwise.shared_pref_config.TokenManger
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Singleton object responsible for configuring and providing an instance of the RentWise API service.
// It sets up the Retrofit client with authentication, logging, and timeout settings for secure and reliable communication with the backend.
object RetrofitInstance {
    // Base URL for all RentWise API requests, ensuring all endpoints are relative to this root.
    private const val BASE_URL = "https://rentwiseapi.onrender.com/"

    // Creates and returns an implementation of the RentWiseApi interface, configured with authentication and logging.
    fun createAPIInstance(context: Context): RentWiseApi {
        // Configures HTTP logging to capture request and response details for debugging.
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Initializes the token manager to retrieve the user's authentication token from shared preferences.
        val tokenManager = TokenManger(context.applicationContext)

        // Builds an OkHttpClient with custom timeouts, logging, and an interceptor to add the Authorization header if a token is present.
        val client = OkHttpClient.Builder()
            .connectTimeout(50, TimeUnit.SECONDS) // Sets connection timeout to 50 seconds to accommodate backend cold starts.
            .readTimeout(50, TimeUnit.SECONDS)    // Sets read timeout to 50 seconds for slow responses.
            .writeTimeout(50, TimeUnit.SECONDS)   // Sets write timeout to 50 seconds for large payloads.
            .addInterceptor(logging)              // Adds logging interceptor for HTTP traffic.
            .addInterceptor { chain ->
                // Build the request with Accept header and Authorization when available
                val original = chain.request()
                val token = tokenManager.getToken()
                val requestBuilder = original.newBuilder()
                    .addHeader("Accept", "application/json")

                if (!token.isNullOrEmpty()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }

                val request = requestBuilder.build()

                // Proceed with the request and inspect the response
                val response = chain.proceed(request)

                // If the server returned 401 Unauthorized, clear stored auth data so the app can redirect to login
                if (response.code == 401) {
                    try {
                        tokenManager.clearToken()
                        tokenManager.clearUser()
                        tokenManager.clearPfp()
                    } catch (e: Exception) {
                        // Swallow any exceptions from token clearing to avoid breaking the interceptor
                    }
                }

                response
            }
            .build()

        // Constructs and returns a Retrofit instance configured for the RentWise API.
        return Retrofit.Builder()
            .baseUrl(BASE_URL)                        // Sets the API base URL.
            .client(client)                           // Attaches the custom OkHttpClient.
            .addConverterFactory(GsonConverterFactory.create()) // Enables JSON serialization/deserialization.
            .build()
            .create(RentWiseApi::class.java)          // Creates the API service interface implementation.
    }
}
