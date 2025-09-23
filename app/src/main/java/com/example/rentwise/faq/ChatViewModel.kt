package com.example.rentwise.faq

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rentwise.data_classes.ChatRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


// Holds all UI data
//Still working on this for part 3 implementation
data class ChatUiState(
    val messages: List<ChatRequest.Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    // User sends a message
    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val currentMsgs = _uiState.value.messages.toMutableList()
        currentMsgs.add(ChatRequest.Message("user", text))

        _uiState.value = _uiState.value.copy(
            messages = currentMsgs,
            isLoading = true,
            error = null
        )

        // Call API in coroutine
        viewModelScope.launch {
            val result = repository.sendMessage(currentMsgs)

            result.fold(
                onSuccess = { reply ->
                    val updatedMsgs = currentMsgs.toMutableList()
                    updatedMsgs.add(ChatRequest.Message("assistant", reply))

                    _uiState.value = _uiState.value.copy(
                        messages = updatedMsgs,
                        isLoading = false
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}