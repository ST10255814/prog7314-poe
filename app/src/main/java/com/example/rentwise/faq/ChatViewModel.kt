package com.example.rentwise.faq

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentwise.data_classes.ChatRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Represents the UI state for the chat screen, including the message history, loading status, and any error messages.
data class ChatUiState(
    val messages: List<ChatRequest.Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// ViewModel responsible for managing chat interactions, updating UI state, and handling asynchronous API calls.
class ChatViewModel : ViewModel() {
    // Provides access to chat-related API operations.
    private val repository = ChatRepository()

    // Holds the current UI state and exposes it as an observable flow for the UI to react to changes.
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    // Handles sending a user message, updating the UI state, and processing the AI's response asynchronously.
    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // Appends the user's message to the current message list.
        val currentMsgs = _uiState.value.messages.toMutableList()
        currentMsgs.add(ChatRequest.Message("user", text))
        // Add loading message
        currentMsgs.add(ChatRequest.Message("loading", ""))

        // Updates the UI to reflect the new message and show a loading indicator.
        _uiState.value = _uiState.value.copy(
            messages = currentMsgs,
            isLoading = true,
            error = null
        )

        // Launches a coroutine to send the message to the AI and handle the response.
        viewModelScope.launch {
            val result = repository.sendMessage(currentMsgs.filter { it.role != "loading" })

            result.fold(
                onSuccess = { reply ->
                    // Remove loading message and append the AI's reply
                    val updatedMsgs = _uiState.value.messages.toMutableList()
                    val loadingIndex = updatedMsgs.indexOfLast { it.role == "loading" }
                    if (loadingIndex != -1) updatedMsgs.removeAt(loadingIndex)
                    updatedMsgs.add(ChatRequest.Message("ai", reply))

                    _uiState.value = _uiState.value.copy(
                        messages = updatedMsgs,
                        isLoading = false
                    )
                },
                onFailure = { e ->
                    // Remove loading message and update error
                    val updatedMsgs = _uiState.value.messages.toMutableList()
                    val loadingIndex = updatedMsgs.indexOfLast { it.role == "loading" }
                    if (loadingIndex != -1) updatedMsgs.removeAt(loadingIndex)
                    _uiState.value = _uiState.value.copy(
                        messages = updatedMsgs,
                        isLoading = false,
                        error = e.message
                    )
                }
            )
        }
    }

    // Clears any error messages from the UI state, typically after they have been displayed to the user.
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
