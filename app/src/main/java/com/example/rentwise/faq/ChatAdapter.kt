package com.example.rentwise.faq

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.rentwise.R
import com.example.rentwise.data_classes.ChatRequest

//Still working on this for part 3 implementation
class ChatAdapter : ListAdapter<ChatRequest.Message, ChatAdapter.MessageViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_AI = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).role == "user") VIEW_TYPE_USER else VIEW_TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutId = if (viewType == VIEW_TYPE_USER) {
            R.layout.faq_message_user
        } else {
            R.layout.faq_message_bot
        }

        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tv_message)

        fun bind(message: ChatRequest.Message) {
            messageText.text = message.content
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ChatRequest.Message>() {
        override fun areItemsTheSame(oldItem: ChatRequest.Message, newItem: ChatRequest.Message): Boolean {
            return oldItem.content == newItem.content && oldItem.role == newItem.role
        }

        override fun areContentsTheSame(oldItem: ChatRequest.Message, newItem: ChatRequest.Message): Boolean {
            return oldItem == newItem
        }
    }
}