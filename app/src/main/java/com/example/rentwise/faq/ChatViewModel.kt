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

        // Updates the UI to reflect the new message and show a loading indicator.
        _uiState.value = _uiState.value.copy(
            messages = currentMsgs,
            isLoading = true,
            error = null
        )

        // Launches a coroutine to send the message to the AI and handle the response.
        viewModelScope.launch {
            val result = repository.sendMessage(currentMsgs)

            result.fold(
                onSuccess = { reply ->
                    // Appends the AI's reply to the message list and updates the UI state.
                    val updatedMsgs = currentMsgs.toMutableList()
                    updatedMsgs.add(ChatRequest.Message("assistant", reply))

                    _uiState.value = _uiState.value.copy(
                        messages = updatedMsgs,
                        isLoading = false
                    )
                },
                onFailure = { e ->
                    // Updates the UI state to reflect the error and stop loading.
                    _uiState.value = _uiState.value.copy(
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
