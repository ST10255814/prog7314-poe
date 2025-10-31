package com.example.rentwise.Payments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.rentwise.booking.BookingStatus
import com.example.rentwise.custom_toast.CustomToast
import com.example.rentwise.databinding.ActivityPaymentBinding
import com.example.rentwise.shared_pref_config.PaymentStore
import com.example.rentwise.shared_pref_config.TokenManger
import com.example.rentwise.utils.LocaleHelper

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private lateinit var tokenManger: TokenManger
    private lateinit var paymentStore: PaymentStore

    // <------THIS WAS CHANGED-----> OVERRIDE ATTACHBASECONTEXT TO APPLY SAVED LOCALE
// This ensures the saved language is applied when the activity is created
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManger = TokenManger(applicationContext)
        paymentStore = PaymentStore(applicationContext)

        // If there is no persisted summary yet, create a minimal one from Intent extras
        if (paymentStore.get() == null) {
            val tmp = PaymentSummary(
                bookingId = intent.getStringExtra("bookingId") ?: "",
                listingId = intent.getStringExtra("listingId") ?: "",
                propertyName = intent.getStringExtra("propertyName") ?: "Property",
                checkIn = intent.getStringExtra("checkIn") ?: "-",
                checkOut = intent.getStringExtra("checkOut") ?: "-",
                amount = (intent.getStringExtra("amount") ?: "0.00").replace(",","").trim(),
                paymentStatus = PaymentStatus.NOT_PAID
            )
            paymentStore.save(tmp)
        }

        bindSummaryFromStoreOrIntent()
        setListeners()
        setupRadioBehaviour()
        setupCardFormatWatcher()
    }

    private fun bindSummaryFromStoreOrIntent() {
        val persisted = paymentStore.get()
        val propertyName = persisted?.propertyName ?: intent.getStringExtra("propertyName") ?: "Property"
        val checkIn = persisted?.checkIn ?: intent.getStringExtra("checkIn") ?: "-"
        val checkOut = persisted?.checkOut ?: intent.getStringExtra("checkOut") ?: "-"
        val amountRaw = persisted?.amount ?: intent.getStringExtra("amount") ?: "0.00"
        val amount = amountRaw.replace(",","").trim()
        val status = persisted?.paymentStatus ?: PaymentStatus.NOT_PAID

        binding.summaryProperty.text = propertyName
        binding.summaryDates.text = "$checkIn  ➜  $checkOut"
        binding.summaryAmount.text = "R$amount"

        when (status) {
            PaymentStatus.NOT_PAID -> {
                binding.paymentStatusText.text = "Payment Status: Not Paid"
                binding.btnPayNow.isEnabled = true
            }
            PaymentStatus.PROCESSING -> {
                binding.paymentStatusText.text = "Payment Status: Processing..."
                binding.btnPayNow.isEnabled = false
            }
            PaymentStatus.PAID -> {
                binding.paymentStatusText.text = "Payment Status: Paid ✓"
                binding.btnPayNow.isEnabled = false
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        binding.backButton.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.backButton.setOnTouchListener { v, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
            false
        }

        binding.btnPayNow.setOnClickListener { processPayment() }
        binding.btnPayNow.setOnTouchListener { v, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
            }
            false
        }
    }

    private fun setupRadioBehaviour() { /* unchanged */ }
    private fun setupCardFormatWatcher() { /* unchanged */ }

    private fun processPayment() {
        paymentStore.setStatus(PaymentStatus.PROCESSING)
        bindSummaryFromStoreOrIntent()
        showProcessing()

        binding.root.postDelayed({
            paymentStore.setStatus(PaymentStatus.PAID)
            hideProcessing()
            bindSummaryFromStoreOrIntent()
            CustomToast.show(this, "Payment successful", CustomToast.Companion.ToastType.SUCCESS)
            startActivity(Intent(this, BookingStatus::class.java))
            finish()
        }, 1200)
    }

    private fun showProcessing() { binding.paymentProcessingOverlay.visibility = View.VISIBLE }
    private fun hideProcessing() { binding.paymentProcessingOverlay.visibility = View.GONE }
}
