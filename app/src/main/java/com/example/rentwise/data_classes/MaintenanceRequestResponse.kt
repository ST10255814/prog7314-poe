package com.example.rentwise.data_classes

import com.google.gson.annotations.SerializedName

data class MaintenanceRequestResponse(
    @SerializedName("_id")
    val maintenanceId: String?,
    val userId: String?,
    val listingDetail: ListingResponse?,
    val bookingId: String?,
    val newMaintenanceRequest: NewMaintenanceRequest?,
    val assignedCaretaker: String?,
    val followUps: Int?,
)

