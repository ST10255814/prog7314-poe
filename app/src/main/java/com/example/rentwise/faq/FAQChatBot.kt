package com.example.rentwise.faq

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentwise.adapters.ChatAdapter
import com.example.rentwise.databinding.ActivityFaqchatBotBinding
import com.example.rentwise.home.HomeScreen
import kotlinx.coroutines.launch

// Activity responsible for managing the FAQ chatbot interface, including message input, display, and AI interaction.
class FAQChatBot : AppCompatActivity() {

    // Binds the layout views for the chatbot screen.
    private lateinit var binding: ActivityFaqchatBotBinding
    // Provides the ViewModel for managing chat state and API calls.
    private val chatViewModel: ChatViewModel by viewModels()
    // Adapter for displaying chat messages in a RecyclerView.
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFaqchatBotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView() // Initializes the RecyclerView for chat messages.
        setListeners() // Attaches all event listeners for user interaction.
        observeViewModel() // Observes ViewModel state to update UI in real time.
    }

    // Configures the RecyclerView to display chat messages using the ChatAdapter.
    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.rvChatMessages.apply {
            layoutManager = LinearLayoutManager(this@FAQChatBot)
            adapter = chatAdapter
        }
    }

    // Observes the ViewModel's UI state and updates the chat UI, including error handling and loading state.
    private fun observeViewModel() {
        lifecycleScope.launch {
            chatViewModel.uiState.collect { state: ChatUiState ->
                chatAdapter.submitList(state.messages.toList()) // Ensures DiffUtil works with a new list.

                // Automatically scrolls to the latest message for better user experience.
                if (state.messages.isNotEmpty()) {
                    binding.rvChatMessages.scrollToPosition(state.messages.size - 1)
                }

                // Disables the send button and changes its appearance while loading.
                binding.btnSend.isEnabled = !state.isLoading
                binding.btnSend.alpha = if (state.isLoading) 0.5f else 1.0f

                // Displays error messages as toasts and clears them from the ViewModel.
                state.error?.let { errorMsg ->
                    Toast.makeText(this@FAQChatBot, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                    chatViewModel.clearError()
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    // Sets up listeners for sending messages, button animations, and navigation.
    private fun setListeners() {

        // Handles send button click to send user messages to the ViewModel.
        binding.btnSend.setOnClickListener {
            val userMessage = binding.etMessage.text.toString().trim()
            if (userMessage.isNotBlank()) {
                chatViewModel.sendMessage(userMessage)
                binding.etMessage.text.clear()
            }
        }

        // Animates the send button on touch for visual feedback.
        binding.btnSend.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            false
        }

        // Animates the back button on touch for visual feedback.
        binding.btnBack.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            false
        }

        // Handles back button click to navigate to the home screen.
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
            finish()
        }

        // Sends a message when the user presses Enter in the message input field.
        binding.etMessage.setOnEditorActionListener { _, _, _ ->
            val userMessage = binding.etMessage.text.toString().trim()
            if (userMessage.isNotBlank()) {
                chatViewModel.sendMessage(userMessage)
                binding.etMessage.text.clear()
            }
            true
        }
    }
}
