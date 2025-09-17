package com.example.rentwise.data_classes

import com.google.gson.annotations.SerializedName

data class BookingResponse(
    val message: String?
)

data class BookingStatusResponse(
    val newBooking: NewBookingStatus?,
    @SerializedName("_id")
    val bookingId: String?,
)

data class NewBookingStatus (
    val status: String?
)
