package com.example.rentwise.Payments

// Small DTO to persist & pass between Booking, Status, and Payment screens
data class PaymentSummary(
    val bookingId: String = "",
    val listingId: String = "",
    val propertyName: String = "",
    val checkIn: String = "",
    val checkOut: String = "",
    val amount: String = "0.00",
    val paymentStatus: String = PaymentStatus.NOT_PAID
)