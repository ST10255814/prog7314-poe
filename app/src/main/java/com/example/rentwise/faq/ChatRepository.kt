package com.example.rentwise.faq

import android.util.Log
import com.example.rentwise.data_classes.ChatRequest
import com.example.rentwise.retrofit_instance.OpenRouterInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Repository responsible for handling chat interactions with the AI backend, ensuring system context and error management.
class ChatRepository {
    // Initializes the API interface for sending chat requests to the OpenRouter backend.
    private val api = OpenRouterInstance.createAPI()

    // Sends a list of chat messages to the AI, always ensuring a system prompt is included, and returns the AI's response or error.
    suspend fun sendMessage(messages: List<ChatRequest.Message>): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Ensures the first message is a system prompt for consistent AI context; prepends if missing.
                val fullMessages = if (messages.firstOrNull()?.role == "system") {
                    messages
                } else {
                    listOf(ChatRequest.Message("system", "You are a helpful assistant.")) + messages
                }

                // Defines a detailed system message for property management context (currently unused in the request).
                val systemMessage = ChatRequest.Message(
                    role = "System",
                    content = "Your name is Astral, you are a property management Assistant AI. Currently you are in a mobile app where people can view and book" +
                            "holiday houses, you can give advice on all ethical and legal questions regarding this subject"
                )

                // Constructs the chat request with the specified model and the prepared message list.
                val request = ChatRequest.ChatRequest(
                    model = "nvidia/nemotron-nano-9b-v2:free",
                    messages = fullMessages
                )

                // Executes the API call to send the chat request and waits for the response.
                val response = api.sendMessage(request)

                // Processes a successful response, extracting the AI's reply or a fallback message if absent.
                if (response.isSuccessful) {
                    val chatResponse: ChatRequest.ChatResponse? = response.body()
                    val aiMessage = chatResponse?.choices?.firstOrNull()?.message?.content
                        ?: "No response from model"

                    Result.success(aiMessage)
                } else {
                    // Detailed logging to help debug 401 / user not found errors from OpenRouter
                    val code = response.code()
                    val headers = response.headers().toString()
                    val errorBodyText = try { response.errorBody()?.string() } catch (e: Exception) { "<error reading body>" }

                    Log.e("OpenRouter", "API Error code=$code")
                    Log.e("OpenRouter", "Headers: $headers")
                    Log.e("OpenRouter", "Error body: $errorBodyText")

                    // Return a failure with details so callers can surface it
                    Result.failure(Exception("API Error $code - $errorBodyText"))
                }
            } catch (e: Exception) {
                // Catches and returns any unexpected exceptions during the network operation, and logs for debugging.
                Log.e("OpenRouter", "Exception sending chat request", e)
                Result.failure(e)
            }
        }
    }
}
