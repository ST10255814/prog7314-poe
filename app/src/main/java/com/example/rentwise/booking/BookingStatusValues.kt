package com.example.rentwise.booking

// Centralized booking status constants + helpers to avoid magic strings throughout the app.
object BookingStatusValues {
    const val PENDING = "pending"
    const val UNDER_REVIEW = "under review"
    const val FINAL_DECISION = "final decision"
    const val APPROVED = "approved"
    const val REJECTED = "rejected"
    const val ACTIVE = "active"

    fun isApprovedLike(status: String?): Boolean {
        val s = status?.lowercase() ?: return false
        return s == APPROVED || s == ACTIVE
    }
}
