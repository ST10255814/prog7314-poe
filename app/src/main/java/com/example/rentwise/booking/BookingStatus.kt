package com.example.rentwise.booking

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.rentwise.R
import com.example.rentwise.databinding.ActivityBookingStatusBinding
import com.example.rentwise.home.HomeScreen

class BookingStatus : AppCompatActivity() {
    private lateinit var binding: ActivityBookingStatusBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBookingStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()
        prepBookingTracker()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners(){
        binding.backButton.setOnClickListener {
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
            finish()
        }
        binding.backButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            false
        }
    }

    private fun prepBookingTracker() {
        val stepNames = listOf(
            "Application Submitted",
            "Application Reviewed",
            "Lease Offer Sent",
            "Lease Signed",
            "Deposit & Rent Paid",
            "Move-In Inspection",
            "Keys Handed Over",
            "Tenancy Active"
        )

        val stepViews = listOf(
            findViewById<View>(R.id.step1),
            findViewById<View>(R.id.step2),
            findViewById<View>(R.id.step3),
            findViewById<View>(R.id.step4),
            findViewById<View>(R.id.step5),
            findViewById<View>(R.id.step6),
            findViewById<View>(R.id.step7),
            findViewById<View>(R.id.step8)
        )

        val currentStep = 4

        stepViews.forEachIndexed { index, container ->
            val label = container.findViewById<TextView>(R.id.stepLabel)
            val icon = container.findViewById<ImageView>(R.id.stepIcon)

            label.text = stepNames[index]

            when {
                index < currentStep - 1 -> { // Completed
                    icon.isSelected = true
                    icon.isActivated = false
                    label.isSelected = true
                    label.isActivated = false
                }
                index == currentStep - 1 -> { // In Progress
                    icon.isSelected = false
                    icon.isActivated = true
                    label.isSelected = false
                    label.isActivated = true
                }
                else -> { // Pending
                    icon.isSelected = false
                    icon.isActivated = false
                    label.isSelected = false
                    label.isActivated = false
                }
            }
        }

        binding.progressBar.progress =
            ((currentStep - 1).toFloat() / (stepViews.size - 1) * 100).toInt()

        binding.progressSubtitle.text = "Step $currentStep of ${stepViews.size}: ${stepNames[currentStep - 1]}"
    }
}
