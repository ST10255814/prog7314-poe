package com.example.rentwise.data_classes

class ChatRequest {
    // Request model
    data class ChatRequest(
        val model: String,
        val messages: List<Message>,
        val temperature: Double = 0.7,
        val max_tokens: Int = 512
    )

    // Message model
    data class Message(
        val role: String,   // must be "system", "user", or "assistant"
        val content: String
    )

    // Response model
    data class ChatResponse(
        val id: String,
        val choices: List<Choice>
    )

    data class Choice(
        val message: Message    // OpenRouter uses same Message structure
    )
}