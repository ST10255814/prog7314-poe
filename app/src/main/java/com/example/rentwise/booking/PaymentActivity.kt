package com.example.rentwise.booking

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.rentwise.R
import com.example.rentwise.auth.LoginActivity
import com.example.rentwise.custom_toast.CustomToast
import com.example.rentwise.databinding.ActivityPaymentBinding
import com.example.rentwise.home.HomeScreen
import com.example.rentwise.shared_pref_config.TokenManger

// Lightweight payment portal shown after booking; supports Card/EFT/Pay-on-Arrival.
// Keeps your styling (fonts, colors, Material cards, overlays) and uses CustomToast.
class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private lateinit var tokenManger: TokenManger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManger = TokenManger(applicationContext)

        bindSummaryFromIntent()
        setListeners()
        setupRadioBehaviour()
        setupCardFormatWatcher()
    }

    private fun bindSummaryFromIntent() {
        val propertyName = intent.getStringExtra("propertyName") ?: "Property"
        val checkIn = intent.getStringExtra("checkIn") ?: "-"
        val checkOut = intent.getStringExtra("checkOut") ?: "-"
        val amount = intent.getStringExtra("amount") ?: "0.00"

        binding.summaryProperty.text = propertyName
        binding.summaryDates.text = "$checkIn  ➜  $checkOut"
        binding.summaryAmount.text = "R$amount"
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.backButton.setOnTouchListener { v, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
            false
        }

        binding.btnPayNow.setOnClickListener {
            processPayment() // simulate + validate client-side card/EFT details
        }
        binding.btnPayNow.setOnTouchListener { v, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
            }
            false
        }
    }

    private fun setupRadioBehaviour() {
        binding.methodCard.setOnCheckedChangeListener { _, checked ->
            binding.cardFields.visibility = if (checked) View.VISIBLE else View.GONE
        }
        binding.methodEft.setOnCheckedChangeListener { _, checked ->
            if (checked) binding.cardFields.visibility = View.GONE
        }
        binding.methodArrival.setOnCheckedChangeListener { _, checked ->
            if (checked) binding.cardFields.visibility = View.GONE
        }
        // default card
        binding.methodCard.isChecked = true
        binding.cardFields.visibility = View.VISIBLE
    }

    private fun setupCardFormatWatcher() {
        binding.editCardNumber.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true
                val digits = s.toString().replace("\\s".toRegex(), "")
                val grouped = digits.chunked(4).joinToString(" ")
                binding.editCardNumber.setText(grouped)
                binding.editCardNumber.setSelection(grouped.length)
                isFormatting = false
            }
        })
    }

    private fun processPayment() {
        // In absence of a backend payment API, we do client-side validation + simulate success.
        // If you add a gateway later, plug the API here and keep the overlay UX the same.

        val usingCard = binding.methodCard.isChecked
        if (usingCard) {
            val number = binding.editCardNumber.text?.toString()?.replace(" ", "") ?: ""
            val name = binding.editCardName.text?.toString()?.trim().orEmpty()
            val expiry = binding.editCardExpiry.text?.toString()?.trim().orEmpty()
            val cvv = binding.editCardCvv.text?.toString()?.trim().orEmpty()

            if (number.length < 12 || name.isEmpty() || !expiry.matches(Regex("^(0[1-9]|1[0-2])/[0-9]{2}$")) || cvv.length !in 3..4) {
                CustomToast.show(this, "Please enter valid card details", CustomToast.Companion.ToastType.ERROR)
                return
            }
        }

        showProcessing()
        binding.btnPayNow.isEnabled = false

        // Simulate processing
        binding.root.postDelayed({
            hideProcessing()
            binding.btnPayNow.isEnabled = true

            CustomToast.show(this, "Payment successful", CustomToast.Companion.ToastType.SUCCESS)

            // After payment, show booking status screen (keeps your existing flow)
            val intent = Intent(this, BookingStatus::class.java) // could pass refs if needed
            startActivity(intent)
            finish()
        }, 1200)
    }

    // Overlays similar to your style
    private fun showProcessing() { binding.paymentProcessingOverlay.visibility = View.VISIBLE }
    private fun hideProcessing() { binding.paymentProcessingOverlay.visibility = View.GONE }
}
