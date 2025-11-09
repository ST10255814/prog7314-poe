package com.example.rentwise.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rentwise.R
import com.example.rentwise.data_classes.NotificationResponse
import com.example.rentwise.databinding.NotificationItemCardBinding
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

// Adapter responsible for displaying a list of notifications in a RecyclerView.
// Each notification item shows an icon, title, message, and formatted timestamp.
class NotificationAdapter(
    private val notifications: List<NotificationResponse>,
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    // ViewHolder class that holds the binding for each notification item layout.
    class NotificationViewHolder(val binding: NotificationItemCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Inflates the notification item layout and creates a new ViewHolder for each notification.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = NotificationItemCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    // Binds the notification data to the ViewHolder, setting the icon, title, message, and formatted time.
    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]

        holder.binding.apply {
            // Sets a static notification icon for each item.
            notificationIcon.setImageResource(R.drawable.notification_icon)
            // Displays the notification title.
            notificationTitle.text = notification.title
            // Shows the notification message content.
            notificationMessage.text = notification.message
            // Formats and displays the notification creation time in a user-friendly format.
            notificationTime.text = formatIsoDate(notification.createdAt)
        }
    }

    // Returns the total number of notifications to be displayed in the RecyclerView.
    override fun getItemCount(): Int = notifications.size

    // Converts an ISO 8601 date string (from MongoDB) to a human-readable time format.
    // If parsing fails, returns the original string as a fallback.
    private fun formatIsoDate(dateString: String?): String {
        if (dateString.isNullOrBlank()) return ""

        return try {
            // Parses the ISO UTC date string to an Instant.
            val instant = Instant.parse(dateString)
            // Converts the Instant to the device's local time zone.
            val localDateTime = LocalDateTime.ofInstant(
                instant,
                ZoneId.systemDefault()
            )
            // Formats the local date and time to a 12-hour clock with seconds and AM/PM.
            val formatter = DateTimeFormatter.ofPattern(
                "hh:mm:ss a",
                Locale.getDefault()
            )
            localDateTime.format(formatter)
        } catch (e: Exception) {
            dateString
        }
    }
}
