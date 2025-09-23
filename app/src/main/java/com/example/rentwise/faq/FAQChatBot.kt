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
import com.example.rentwise.databinding.ActivityFaqchatBotBinding
import com.example.rentwise.home.HomeScreen
import kotlinx.coroutines.launch

class FAQChatBot : AppCompatActivity() {

    private lateinit var binding: ActivityFaqchatBotBinding
    private val chatViewModel: ChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFaqchatBotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.rvChatMessages.apply {
            layoutManager = LinearLayoutManager(this@FAQChatBot)
            adapter = chatAdapter
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            chatViewModel.uiState.collect { state: ChatUiState ->
                // Update chat messages
                chatAdapter.submitList(state.messages.toList()) // Convert to new list for DiffUtil

                // Auto-scroll to latest message
                if (state.messages.isNotEmpty()) {
                    binding.rvChatMessages.scrollToPosition(state.messages.size - 1)
                }

                // Show/hide loading (disable send button while loading)
                binding.btnSend.isEnabled = !state.isLoading
                binding.btnSend.alpha = if (state.isLoading) 0.5f else 1.0f

                // Handle errors
                state.error?.let { errorMsg ->
                    Toast.makeText(this@FAQChatBot, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                    chatViewModel.clearError()
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {

        // Send button click
        binding.btnSend.setOnClickListener {
            val userMessage = binding.etMessage.text.toString().trim()
            if (userMessage.isNotBlank()) {
                chatViewModel.sendMessage(userMessage)
                binding.etMessage.text.clear()
            }
        }

        // Send button touch animation (your existing code)
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

        // Back button touch animation (your existing code)
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

        // Back button click
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
            finish()
        }

        // Optional: Send message when user presses Enter
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