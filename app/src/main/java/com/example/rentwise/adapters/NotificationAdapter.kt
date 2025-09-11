package com.example.rentwise

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rentwise.data_classes.NotificationResponse
import com.example.rentwise.databinding.NotificationItemCardBinding
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class NotificationAdapter(
    private val notifications: List<NotificationResponse>,
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(val binding: NotificationItemCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = NotificationItemCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]

        holder.binding.apply {
            notificationIcon.setImageResource(R.drawable.notification_icon)
            notificationTitle.text = notification.title
            notificationMessage.text = notification.notificationMessage
            notificationTime.text = formatIsoDate(notification.createdAt)
        }
    }

    override fun getItemCount(): Int = notifications.size

    private fun formatIsoDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return ""

        return try {
            val instant = Instant.parse(dateString) // parse ISO UTC date
            val localDateTime = LocalDateTime.ofInstant(
                instant,
                ZoneId.systemDefault() // convert to device timezone
            )
            val formatter = DateTimeFormatter.ofPattern(
                "hh:mm:ss a",
                Locale.getDefault()
            )
            localDateTime.format(formatter)
        } catch (e: Exception) {
            dateString // fallback
        }
    }
}
