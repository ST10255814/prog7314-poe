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
import com.example.rentwise.shared_pref_config.TokenManger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MaintenanceRequest : AppCompatActivity() {
    private lateinit var binding: ActivityMaintenanceRequestBinding
    private lateinit var adapter: MaintenanceRequestAdapter
    private lateinit var tokenManger: TokenManger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMaintenanceRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManger = TokenManger(applicationContext)

        setListeners()
        getMaintenanceRequestsForUser()
    }
    //api call
    private fun getMaintenanceRequestsForUser() {
        showOverlay()
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
                                showRecyclerView()
                                adapter = MaintenanceRequestAdapter(requests = responseBody)
                                binding.rvRequests.layoutManager = LinearLayoutManager(this@MaintenanceRequest)
                                binding.rvRequests.adapter = adapter
                                CustomToast.show(this@MaintenanceRequest, "Maintenance requests fetched", CustomToast.Companion.ToastType.SUCCESS )
                            } else{
                                showEmptyRecyclerView()
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
                    //Log error
                    hideOverlay()
                    showEmptyRecyclerView()
                    CustomToast.show(this@MaintenanceRequest, "${t.message}", CustomToast.Companion.ToastType.ERROR)
                    Log.e("Failure", "API call failed: ${t.message}" )
                }

            })
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners(){
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
            finish()
        }
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

        binding.refreshTracking.setOnClickListener {
            getMaintenanceRequestsForUser()
        }
    }

    private fun showOverlay(){
        binding.overlayLoadingRequests.visibility = View.VISIBLE
    }
    private fun hideOverlay(){
        binding.overlayLoadingRequests.visibility = View.GONE
    }
    private fun showEmptyRecyclerView(){
        binding.rvRequests.visibility = View.GONE
        binding.emptyView.emptyLayout.visibility = View.VISIBLE
    }
    private fun showRecyclerView(){
        binding.rvRequests.visibility = View.VISIBLE
        binding.emptyView.emptyLayout.visibility = View.GONE
    }
}