package com.example.rentwise.booking

import com.example.rentwise.retrofit_instance.RetrofitInstance
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.rentwise.auth.LoginActivity
import com.example.rentwise.custom_toast.CustomToast
import com.example.rentwise.data_classes.BookingStatusResponse
import com.example.rentwise.databinding.ActivityBookingStatusBinding
import com.example.rentwise.home.HomeScreen
import com.example.rentwise.shared_pref_config.TokenManger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class BookingStatus : AppCompatActivity() {
    // Binds the layout views for the booking status screen and manages user session tokens.
    private lateinit var binding: ActivityBookingStatusBinding
    private lateinit var tokenManger: TokenManger

    // Carry-forward values from Booking screen for Payment later.
    private var summaryAmount: String? = null
    private var summaryProperty: String? = null
    private var summaryCheckIn: String? = null
    private var summaryCheckOut: String? = null
    private var summaryListingId: String? = null
    private var currentBookingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBookingStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManger = TokenManger(applicationContext)

        // Read extras coming from Booking (so we can pass them to Payment later)
        summaryAmount = intent.getStringExtra("amount")
        summaryProperty = intent.getStringExtra("propertyName")
        summaryCheckIn = intent.getStringExtra("checkIn")
        summaryCheckOut = intent.getStringExtra("checkOut")
        summaryListingId = intent.getStringExtra("listingId")

        setListeners() // Attaches all event listeners for navigation and refresh actions.
        getBookingStatusViaUserIdApiCall() // Initiates the API call to fetch and display booking status.
    }

    @SuppressLint("ClickableViewAccessibility")
    // Sets up listeners for navigation, refresh, and button animations.
    private fun setListeners(){
        binding.backButton.setOnClickListener {
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
            finish()
        }
        binding.backButton.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
            false
        }
        binding.refreshTracking.setOnClickListener {
            getBookingStatusViaUserIdApiCall()
            CustomToast.show(this@BookingStatus, "Tracking Refreshed", CustomToast.Companion.ToastType.INFO)
        }
        binding.refreshTracking.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
            false
        }

        // Proceed to Payment CTA (initially GONE; shown when Approved).
        binding.btnProceedToPayment.setOnClickListener {
            val amount = summaryAmount?.takeIf { it.isNotBlank() } ?: run {
                CustomToast.show(this, "Amount unavailable", CustomToast.Companion.ToastType.ERROR)
                return@setOnClickListener
            }
            val pay = Intent(this, PaymentActivity::class.java)
            pay.putExtra("amount", amount)
            pay.putExtra("propertyName", summaryProperty ?: "Property")
            pay.putExtra("checkIn", summaryCheckIn ?: "-")
            pay.putExtra("checkOut", summaryCheckOut ?: "-")
            pay.putExtra("listingId", summaryListingId ?: "")
            pay.putExtra("bookingId", currentBookingId ?: "")
            startActivity(pay)
        }
    }

    @SuppressLint("SetTextI18n")
    // Updates the UI to reflect the current booking status using a step tracker and progress bar.
    private fun prepBookingTracker(status: String, bookingId: String) {
        binding.bookingIdText.text = "Booking ID: $bookingId"
        currentBookingId = bookingId // CACHE FOR ID

        val stepNames = listOf(
            "Pending",
            "Under Review",
            "Awaiting Final Decision",
            when(status.lowercase()) {
                "approved" -> "Approved"
                "rejected" -> "Rejected"
                "active" -> "Active"
                else -> "Outcome"
            }
        )

        val stepViews = listOf(
            binding.step1,
            binding.step2,
            binding.step3,
            binding.step4
        )

        val currentStep = when(status.lowercase()) {
            "pending" -> 1
            "under review" -> 2
            "final decision" -> 3
            "approved", "rejected", "active" -> 4   // treat active as final too
            else -> 1
        }

        stepViews.forEachIndexed { index, container ->
            val label = container.stepLabel
            val icon = container.stepIcon

            label.text = stepNames[index]

            when {
                index < currentStep - 1 -> {
                    icon.isSelected = true
                    icon.isActivated = false
                    label.isSelected = true
                    label.isActivated = false
                }
                index == currentStep - 1 -> {
                    if (status.lowercase() == "approved" || status.lowercase() == "rejected" || status.lowercase() == "active") {
                        icon.isSelected = true
                        icon.isActivated = false
                        label.isSelected = true
                        label.isActivated = false
                    } else {
                        icon.isSelected = false
                        icon.isActivated = true
                        label.isSelected = false
                        label.isActivated = true
                    }
                }
                else -> {
                    icon.isSelected = false
                    icon.isActivated = false
                    label.isSelected = false
                    label.isActivated = false
                }
            }
        }
        binding.progressBar.progress = ((currentStep.toFloat() / stepViews.size) * 100).toInt()
        binding.progressSubtitle.text = "Step $currentStep of ${stepViews.size}: ${stepNames[currentStep - 1]}"

        // Show/Hide the Payment CTA depending on status
        val approvedLike = status.equals("approved", true) || status.equals("active", true)
        binding.btnProceedToPayment.visibility = if (approvedLike) View.VISIBLE else View.GONE
    }

    // Fetches the booking status for the current user from the API and updates the UI, handling authentication errors.
    private fun getBookingStatusViaUserIdApiCall(){
        showOverlay()
        val userId = tokenManger.getUser()
        if(userId != null) {
            val api = RetrofitInstance.createAPIInstance(applicationContext)
            api.getBookingById(userId).enqueue( object : Callback<BookingStatusResponse> {
                override fun onResponse(
                    call: Call<BookingStatusResponse>,
                    response: Response<BookingStatusResponse>
                ) {
                    if(response.isSuccessful){
                        hideOverlay()
                        val bookingStatusResponse = response.body()
                        if (bookingStatusResponse != null) {
                            val status = bookingStatusResponse.newBooking?.status
                            val bookingId = bookingStatusResponse.newBooking?.bookingId ?: ""
                            if (status != null) {
                                prepBookingTracker(status, bookingId)
                            }
                        }
                        hideMiddleOverlay()
                    }
                    else{
                        hideOverlay()
                        showMiddleOverlay()
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = errorBody ?: "Unknown error"
                        if(response.code() == 401) {
                            tokenManger.clearToken()
                            tokenManger.clearUser()
                            CustomToast.show(this@BookingStatus, errorMessage, CustomToast.Companion.ToastType.ERROR)
                            val intent = Intent(this@BookingStatus, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
                }
                override fun onFailure(call: Call<BookingStatusResponse>, t: Throwable) {
                    hideOverlay()
                    showMiddleOverlay()
                    CustomToast.show(this@BookingStatus, "Error: ${t.message}", CustomToast.Companion.ToastType.ERROR)
                    Log.e("Failure", "API call failed: ${t.message}" )
                }
            })
        }
        return
    }

    // Shows a full-screen overlay to block user interaction during network operations.
    private fun showOverlay(){
        binding.fullScreenOverlay.visibility = View.VISIBLE
    }

    // Hides the full-screen overlay after network operations are complete.
    private fun hideOverlay(){
        binding.fullScreenOverlay.visibility = View.GONE
    }

    // Shows a middle overlay for displaying errors or special states.
    private fun showMiddleOverlay(){
        binding.middleOverlay.visibility = View.VISIBLE
    }

    // Hides the middle overlay to restore normal UI state.
    private fun hideMiddleOverlay(){
        binding.middleOverlay.visibility = View.GONE
    }
}
