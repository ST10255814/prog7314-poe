package com.example.rentwise.Payments

// Centralized payment status constants to avoid magic strings and to persist progress
object PaymentStatus {
    const val NOT_PAID = "Not Paid"
    const val PROCESSING = "Processing"
    const val PAID = "Booking Paid Successfully"

    fun isPaid(status: String?): Boolean = status == PAID
}
