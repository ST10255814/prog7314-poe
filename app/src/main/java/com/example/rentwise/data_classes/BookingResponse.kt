package com.example.rentwise.data_classes


data class BookingResponse(
    val message: String?
)

data class BookingStatusResponse(
    val newBooking: NewBookingStatus?
)

data class NewBookingStatus (
    val status: String?,
    val bookingId: String?
)
