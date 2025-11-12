package com.example.rentwise.data_classes


data class NewMaintenanceRequest(
    val maintenanceId: String?,
    val issue: String?,
    val description: String?,
    val priority: String?,
    val documentsURL: List<String>?,
    val status: String?,
    val createdAt: String?,
    val caretakerId: String?,
    val caretakerNotes: String?,
    val followUps: Int?,
)