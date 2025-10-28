package com.example.rentwise.shared_pref_config

import android.content.Context
import com.example.rentwise.Payments.PaymentSummary
import com.example.rentwise.Payments.PaymentStatus
import org.json.JSONObject

class PaymentStore(context: Context) {
    private val prefs = context.getSharedPreferences("payment_store", Context.MODE_PRIVATE)

    fun save(summary: PaymentSummary) {
        val json = JSONObject()
            .put("bookingId", summary.bookingId)
            .put("listingId", summary.listingId)
            .put("propertyName", summary.propertyName)
            .put("checkIn", summary.checkIn)
            .put("checkOut", summary.checkOut)
            .put("amount", summary.amount)
            .put("paymentStatus", summary.paymentStatus)
            .toString()
        prefs.edit().putString("summary", json).apply()
    }

    fun get(): PaymentSummary? {
        val s = prefs.getString("summary", null) ?: return null
        return try {
            val o = JSONObject(s)
            PaymentSummary(
                bookingId = o.optString("bookingId",""),
                listingId = o.optString("listingId",""),
                propertyName = o.optString("propertyName",""),
                checkIn = o.optString("checkIn",""),
                checkOut = o.optString("checkOut",""),
                amount = o.optString("amount","0.00"),
                paymentStatus = o.optString("paymentStatus", PaymentStatus.NOT_PAID)
            )
        } catch (_: Throwable) { null }
    }

    fun setStatus(status: String) {
        val current = get() ?: return
        save(current.copy(paymentStatus = status))
    }

    fun clear() {
        prefs.edit().remove("summary").apply()
    }
}
