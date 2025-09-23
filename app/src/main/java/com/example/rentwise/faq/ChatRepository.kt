package com.example.rentwise.faq

import com.example.rentwise.data_classes.ChatRequest
import com.example.rentwise.retrofit_instance.OpenRouterInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepository {
    private val api = OpenRouterInstance.createAPI()

    suspend fun sendMessage(messages: List<ChatRequest.Message>): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Always prepend system message
                val fullMessages = if (messages.firstOrNull()?.role == "system") {
                    messages
                } else {
                    listOf(ChatRequest.Message("system", "You are a helpful assistant.")) + messages
                }

                val systemMessage = ChatRequest.Message(
                    role = "System",
                    content = "Your name is Astral, you are a property management Assistant AI. Currently you are in a mobile app where people can view and book" +
                            "holiday houses, you can give advice on all ethical and legal questions regarding this subject"
                )

                val request = ChatRequest.ChatRequest(
                    model = "nvidia/nemotron-nano-9b-v2:free",
                    messages = fullMessages
                )

                val response = api.sendMessage(request)

                if (response.isSuccessful) {
                    val chatResponse: ChatRequest.ChatResponse? = response.body()
                    val aiMessage = chatResponse?.choices?.firstOrNull()?.message?.content
                        ?: "No response from model"

                    Result.success(aiMessage)
                } else {
                    Result.failure(Exception("API Error ${response.code()} - ${response.errorBody()?.string()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}