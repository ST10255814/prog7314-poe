package com.example.rentwise.faq

import com.example.rentwise.data_classes.ChatRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

//Still working on this for part 3 implementation
interface OpenRouterApiService {

    @Headers(
        "Content-Type: application/json",
        "HTTP-Referer: app://rentwise",   // 👈 Required (fake scheme is fine)
        "X-Title: RentWise FAQ ChatBot"   // 👈 Optional app name
    )
    @POST("chat/completions")
    suspend fun sendMessage(
        @Body request: ChatRequest.ChatRequest
    ): Response<ChatRequest.ChatResponse>
}