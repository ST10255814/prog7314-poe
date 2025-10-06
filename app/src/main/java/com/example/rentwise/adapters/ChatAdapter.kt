package com.example.rentwise.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rentwise.R
import com.example.rentwise.data_classes.ChatRequest

// Adapter for displaying chat messages in a RecyclerView, supporting both user and AI messages with different layouts.
class ChatAdapter : ListAdapter<ChatRequest.Message, ChatAdapter.MessageViewHolder>(DiffCallback()) {

    companion object {
        // Constant representing the view type for user messages.
        private const val VIEW_TYPE_USER = 1
        // Constant representing the view type for AI (bot) messages.
        private const val VIEW_TYPE_AI = 2
    }

    // Determines the view type for a given position based on the message role.
    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).role == "user") VIEW_TYPE_USER else VIEW_TYPE_AI
    }

    // Inflates the appropriate layout for the message based on its view type (user or AI).
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutId = if (viewType == VIEW_TYPE_USER) {
            R.layout.faq_message_user
        } else {
            R.layout.faq_message_bot
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MessageViewHolder(view)
    }

    // Binds the message data to the ViewHolder for display.
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // ViewHolder class responsible for holding and binding the message view.
    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tv_message)

        // Sets the message content to the TextView for display.
        fun bind(message: ChatRequest.Message) {
            messageText.text = message.content
        }
    }

    // DiffUtil callback for efficiently updating the RecyclerView when the message list changes.
    class DiffCallback : DiffUtil.ItemCallback<ChatRequest.Message>() {
        // Checks if two messages represent the same item based on content and role.
        override fun areItemsTheSame(oldItem: ChatRequest.Message, newItem: ChatRequest.Message): Boolean {
            return oldItem.content == newItem.content && oldItem.role == newItem.role
        }

        // Checks if the contents of two messages are the same.
        override fun areContentsTheSame(oldItem: ChatRequest.Message, newItem: ChatRequest.Message): Boolean {
            return oldItem == newItem
        }
    }
}
