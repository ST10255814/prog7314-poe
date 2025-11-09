// Defines the Retrofit API interface for communicating with the OpenRouter backend, specifically for chat-based AI completions.
package com.example.rentwise.faq

import com.example.rentwise.data_classes.ChatRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

// Specifies the contract for sending chat requests to the OpenRouter API, including required HTTP headers for identification and content type.
interface OpenRouterApiService {

    // Declares the HTTP headers necessary for the API call, such as content type and app identification.
    @Headers(
        "Content-Type: application/json",
        "Referer: app://rentwise",   // Identifies the app making the request; a custom scheme is acceptable.
        "X-Title: RentWise FAQ ChatBot"   // Optionally provides the app's name for backend context.
    )
    // Defines a POST request to the /chat/completions endpoint, sending a chat request and expecting a chat response.
    @POST("chat/completions")
    suspend fun sendMessage(
        @Body request: ChatRequest.ChatRequest
    ): Response<ChatRequest.ChatResponse>
}
