package com.example.rentwise

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rentwise.data_classes.NotificationResponse
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class NotificationAdapter(
    private val notifications: List<NotificationResponse>,
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.notification_icon)
        val title: TextView = itemView.findViewById(R.id.notification_title)
        val message: TextView = itemView.findViewById(R.id.notification_message)
        val time: TextView = itemView.findViewById(R.id.notification_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_item_card, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]

        holder.icon.setImageResource(R.drawable.notification_icon)
        holder.title.text = notification.title
        holder.message.text = notification.notificationMessage

        val mongoTime = OffsetDateTime.parse(notification.createdAt)
        val formatter = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.getDefault())
        val formattedTime = mongoTime.format(formatter)
        holder.time.text = formattedTime
    }

    override fun getItemCount(): Int = notifications.size
}
