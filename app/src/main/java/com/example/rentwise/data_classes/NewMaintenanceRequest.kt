package com.example.rentwise.data_classes


data class NewMaintenanceRequest(
    val issue: String?,
    val description: String?,
    val priority: String?,
    val documentsURL: List<String>?,
    val createdAt: String?
)