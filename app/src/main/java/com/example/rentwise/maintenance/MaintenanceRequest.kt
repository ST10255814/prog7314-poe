package com.example.rentwise.maintenance

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentwise.adapters.MaintenanceRequestAdapter
import com.example.rentwise.auth.LoginActivity
import com.example.rentwise.custom_toast.CustomToast
import com.example.rentwise.data_classes.MaintenanceRequestResponse
import com.example.rentwise.databinding.ActivityMaintenanceRequestBinding
import com.example.rentwise.home.HomeScreen
import com.example.rentwise.retrofit_instance.RetrofitInstance
import com.example.rentwise.shared_pref_config.TokenManger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Activity responsible for displaying and managing the user's maintenance requests, including fetching data, handling UI states, and navigation.
class MaintenanceRequest : AppCompatActivity() {
    // Binds the layout for the maintenance request screen, providing access to all UI elements.
    private lateinit var binding: ActivityMaintenanceRequestBinding
    // Adapter for displaying maintenance requests in a RecyclerView.
    private lateinit var adapter: MaintenanceRequestAdapter
    // Manages user authentication tokens and session data.
    private lateinit var tokenManger: TokenManger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMaintenanceRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManger = TokenManger(applicationContext)

        setListeners() // Attaches all event listeners for navigation and refresh actions.
        getMaintenanceRequestsForUser() // Initiates fetching of maintenance requests for the current user.
    }

    // Fetches maintenance requests for the authenticated user, updates the UI based on the response, and handles errors or authentication issues.
    private fun getMaintenanceRequestsForUser() {
        showOverlay() // Displays a loading overlay while fetching data.
        val userId = tokenManger.getUser()
        val api = RetrofitInstance.createAPIInstance(applicationContext)

        if(userId != null) {
            api.getMaintenanceRequestForUser(userId).enqueue(object: Callback<List<MaintenanceRequestResponse>>{
                override fun onResponse(
                    call: Call<List<MaintenanceRequestResponse>?>,
                    response: Response<List<MaintenanceRequestResponse>?>
                ) {
                    if (response.isSuccessful){
                        hideOverlay()
                        val responseBody = response.body()
                        if (responseBody != null) {
                            if (responseBody.isNotEmpty()){
                                showRecyclerView() // Shows the RecyclerView with fetched requests.
                                adapter = MaintenanceRequestAdapter(requests = responseBody)
                                binding.rvRequests.layoutManager = LinearLayoutManager(this@MaintenanceRequest)
                                binding.rvRequests.adapter = adapter
                                CustomToast.show(this@MaintenanceRequest, "Maintenance requests fetched", CustomToast.Companion.ToastType.SUCCESS )
                            } else{
                                showEmptyRecyclerView() // Shows an empty view if there are no requests.
                            }
                        }
                        else{
                            showEmptyRecyclerView()
                        }
                    }
                    else{
                        hideOverlay()
                        showEmptyRecyclerView()
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = errorBody ?: "Unknown error"

                        CustomToast.show(this@MaintenanceRequest, errorMessage, CustomToast.Companion.ToastType.ERROR)

                        // Handles authentication errors by clearing session and redirecting to login.
                        if(response.code() == 401) {
                            tokenManger.clearToken()
                            tokenManger.clearUser()
                            tokenManger.clearPfp()
                            val intent = Intent(this@MaintenanceRequest, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
                }

                override fun onFailure(
                    call: Call<List<MaintenanceRequestResponse>?>,
                    t: Throwable
                ) {
                    // Handles network or unexpected errors, displaying an error message and logging details.
                    hideOverlay()
                    showEmptyRecyclerView()
                    CustomToast.show(this@MaintenanceRequest, "${t.message}", CustomToast.Companion.ToastType.ERROR)
                    Log.e("Failure", "API call failed: ${t.message}" )
                }

            })
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    // Sets up click and touch listeners for navigation and refreshing the maintenance request list.
    private fun setListeners(){
        // Navigates back to the home screen when the back button is clicked.
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
            finish()
        }
        // Animates the back button for visual feedback on touch.
        binding.btnBack.setOnTouchListener { v, event ->
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

        // Animates the refresh button for visual feedback on touch.
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

        // Refreshes the maintenance request list when the refresh button is clicked.
        binding.refreshTracking.setOnClickListener {
            getMaintenanceRequestsForUser()
        }
    }

    // Displays a loading overlay to indicate that data is being fetched.
    private fun showOverlay(){
        binding.overlayLoadingRequests.visibility = View.VISIBLE
    }
    // Hides the loading overlay after data fetching is complete.
    private fun hideOverlay(){
        binding.overlayLoadingRequests.visibility = View.GONE
    }
    // Shows an empty view when there are no maintenance requests to display.
    private fun showEmptyRecyclerView(){
        binding.rvRequests.visibility = View.GONE
        binding.emptyView.emptyLayout.visibility = View.VISIBLE
    }
    // Shows the RecyclerView with maintenance requests and hides the empty view.
    private fun showRecyclerView(){
        binding.rvRequests.visibility = View.VISIBLE
        binding.emptyView.emptyLayout.visibility = View.GONE
    }
}
