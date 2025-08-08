package com.example.rentwise.data_classes

import java.sql.Time

data class NotificationData(
    val notificationTitle: String,
    val notificationMessage: String,
    val notificationTime: Time
)
