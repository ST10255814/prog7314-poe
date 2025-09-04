package com.example.rentwise.data_classes

data class MaintenanceRequestData(
    val id: String,
    val title: String,
    val description: String,
    val priority: String,
    val status: String,
    val unit: String,
    val assignedStaff: String,
    val dateSubmitted: String,
    val followUps: Int,
    val caretakerNote: String
)
