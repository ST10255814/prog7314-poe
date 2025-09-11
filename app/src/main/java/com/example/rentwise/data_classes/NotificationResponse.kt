package com.example.rentwise.data_classes

data class NotificationResponse(
    val _id: String?,
    val title: String?,
    val notificationMessage: String?,
    val isRead: Boolean?,
    val createdAt: String?,
    val message: String?
)
