package com.example.rentwise.faq

import android.util.Log
import com.example.rentwise.data_classes.ChatRequest
import com.example.rentwise.retrofit_instance.OpenRouterInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

// Repository responsible for handling chat interactions with the AI backend, ensuring system context and error management.
class ChatRepository {
    // Initializes the API interface for sending chat requests to the OpenRouter backend.
    private val api = OpenRouterInstance.createAPI()

    // List of free models to try in order of preference
    private val freeModels = listOf(
        "meta-llama/llama-3.3-8b-instruct:free",
        "openai/gpt-oss-20b:free",
        "nvidia/nemotron-nano-9b-v2:free",
        "nvidia/nemotron-nano-12b-v2-vl:free"
    )

    // Sends a list of chat messages to the AI, with retry logic for rate limits and model fallbacks
    suspend fun sendMessage(messages: List<ChatRequest.Message>): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Ensures the first message is a system prompt for consistent AI context; prepends if missing.
                val systemPrompt = ChatRequest.Message(
                    role = "system",
                    content = "Your name is Astral, you are a property management Assistant AI. Currently you are in a mobile app where people can view and book holiday houses. You can give advice on all ethical and legal questions regarding this subject. Be helpful, concise, and professional."
                )

                val fullMessages = if (messages.firstOrNull()?.role == "system") {
                    messages
                } else {
                    listOf(systemPrompt) + messages
                }

                // Try each model in order until one works
                for ((index, model) in freeModels.withIndex()) {
                    Log.d("OpenRouter", "Trying model: $model (attempt ${index + 1}/${freeModels.size})")

                    val result = tryModelWithRetry(model, fullMessages)
                    if (result.isSuccess) {
                        Log.i("OpenRouter", "Successfully used model: $model")
                        return@withContext result
                    } else {
                        Log.w("OpenRouter", "Model $model failed: ${result.exceptionOrNull()?.message}")
                        // If this isn't the last model, continue to next one
                        if (index < freeModels.size - 1) {
                            delay(1000) // Brief delay before trying next model
                        }
                    }
                }

                // If all models failed, return the last failure
                Result.failure(Exception("All available models are currently rate-limited or unavailable. Please try again later."))

            } catch (e: Exception) {
                Log.e("OpenRouter", "Exception sending chat request", e)
                Result.failure(e)
            }
        }
    }

    // Try a specific model with retry logic for temporary failures
    private suspend fun tryModelWithRetry(model: String, messages: List<ChatRequest.Message>, maxRetries: Int = 2): Result<String> {
        repeat(maxRetries) { attempt ->
            try {
                val request = ChatRequest.ChatRequest(
                    model = model,
                    messages = messages
                )

                val response = api.sendMessage(request)

                if (response.isSuccessful) {
                    val chatResponse: ChatRequest.ChatResponse? = response.body()
                    val aiMessage = chatResponse?.choices?.firstOrNull()?.message?.content
                        ?: "No response from model"
                    return Result.success(aiMessage)
                } else {
                    val code = response.code()
                    val errorBodyText = try {
                        response.errorBody()?.string()
                    } catch (e: Exception) {
                        "<error reading body>"
                    }

                    Log.e("OpenRouter", "API Error code=$code for model $model")
                    Log.e("OpenRouter", "Error body: $errorBodyText")

                    // For rate limits (429), don't retry this model - try next model instead
                    if (code == 429) {
                        return Result.failure(Exception("Rate limited: $errorBodyText"))
                    }

                    // For other errors, retry with exponential backoff
                    if (attempt < maxRetries - 1) {
                        val delayMs = (1000 * (attempt + 1)).toLong()
                        Log.i("OpenRouter", "Retrying model $model in ${delayMs}ms...")
                        delay(delayMs)
                    } else {
                        return Result.failure(Exception("API Error $code - $errorBodyText"))
                    }
                }
            } catch (e: Exception) {
                Log.e("OpenRouter", "Exception with model $model, attempt ${attempt + 1}", e)
                if (attempt < maxRetries - 1) {
                    delay(1000 * (attempt + 1).toLong())
                } else {
                    return Result.failure(e)
                }
            }
        }
        return Result.failure(Exception("Max retries exceeded for model $model"))
    }
}
