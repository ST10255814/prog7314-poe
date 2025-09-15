package com.example.rentwise.booking

import RetrofitInstance
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
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
    private lateinit var binding: ActivityBookingStatusBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBookingStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()
        getBookingStatusViaUserIdApiCall()
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
        binding.refreshTracking.setOnClickListener {
            getBookingStatusViaUserIdApiCall()
        }
        binding.refreshTracking.setOnTouchListener { v, event ->
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

    @SuppressLint("SetTextI18n")
    private fun prepBookingTracker(status: String) {

        // List to map step names based on status from api
        val stepNames = listOf(
            "Pending",
            "Under Review",
            when(status.lowercase()) {
                "accepted" -> "Accepted"
                "rejected" -> "Rejected"
                else -> "Final Decision"
            }
        )

        val stepViews = listOf(
            binding.step1,
            binding.step2,
            binding.step3
        )

        // Determine current step based on status
        val currentStep = when(status.lowercase()) {
            "pending" -> 1
            "under review" -> 2
            "accepted", "rejected" -> 3
            else -> 1
        }

        // Update UI for each step
        stepViews.forEachIndexed { index, container ->
            val label = container.stepLabel
            val icon = container.stepIcon

            label.text = stepNames[index]

            when {
                index < currentStep - 1 -> { // Completed steps
                    icon.isSelected = true
                    icon.isActivated = false
                    label.isSelected = true
                    label.isActivated = false
                }
                index == currentStep - 1 -> { // Current step
                    if (status.lowercase() == "accepted" || status.lowercase() == "rejected") {
                        // Mark last step as completed if accepted or rejected
                        icon.isSelected = true
                        icon.isActivated = false
                        label.isSelected = true
                        label.isActivated = false
                    } else {
                        // Mark in-progress for other current steps
                        icon.isSelected = false
                        icon.isActivated = true
                        label.isSelected = false
                        label.isActivated = true
                    }
                }
                else -> { // Pending
                    icon.isSelected = false
                    icon.isActivated = false
                    label.isSelected = false
                    label.isActivated = false
                }
            }
        }
        binding.progressBar.progress = ((currentStep.toFloat() / stepViews.size) * 100).toInt() // Update progress bar
        binding.progressSubtitle.text = "Step $currentStep of ${stepViews.size}: ${stepNames[currentStep - 1]}" // Update subtitle
    }

    private fun getBookingStatusViaUserIdApiCall(){
        showOverlay()
        val tokenManger = TokenManger(applicationContext)

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
                            if (status != null) {
                                prepBookingTracker(status) // Update UI based on status
                            }
                            CustomToast.show(this@BookingStatus, "Booking status fetched!", CustomToast.Companion.ToastType.INFO)
                        }
                    }
                    else{
                        hideOverlay()
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = errorBody ?: "Unknown error"
                        CustomToast.show(this@BookingStatus, errorMessage, CustomToast.Companion.ToastType.ERROR)

                        //Logout user if 401 Unauthorized
                        if(response.code() == 401) {
                            tokenManger.clearToken()
                            tokenManger.clearUser()
                            val intent = Intent(this@BookingStatus, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
                }
                override fun onFailure(call: Call<BookingStatusResponse>, t: Throwable) {
                    // Handle failure
                    hideOverlay()
                    CustomToast.show(this@BookingStatus, "Error: ${t.message}", CustomToast.Companion.ToastType.ERROR)
                    Log.e("Failure", "API call failed: ${t.message}" )
                }
            })
        }
        return
    }

    private fun showOverlay(){
        binding.fullScreenOverlay.visibility = View.VISIBLE
    }

    private fun hideOverlay(){
        binding.fullScreenOverlay.visibility = View.GONE
    }
}
