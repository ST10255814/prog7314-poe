package com.example.rentwise.data_classes

data class MaintenanceRequestResponse(
    val _id: String?,
    val userId: String?,
    val listingDetail: ListingResponse?,
    val bookingId: String?,
    val newMaintenanceRequest: NewMaintenanceRequest?
)

