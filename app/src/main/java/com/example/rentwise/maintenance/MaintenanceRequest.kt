package com.example.rentwise.maintenance

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentwise.adapters.MaintenanceRequestAdapter
import com.example.rentwise.custom_toast.CustomToast
import com.example.rentwise.data_classes.MaintenanceRequestData
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

    }
    //api call
    private fun getMaintenanceRequest() {
        val userId = tokenManger.getUser()
        val api = RetrofitInstance.createAPIInstance(applicationContext)

        if(userId != null) {
            api.getMaintenanceRequestForUser(userId).enqueue(object: Callback<List<MaintenanceRequestResponse>>{
                override fun onResponse(
                    call: Call<List<MaintenanceRequestResponse>?>,
                    response: Response<List<MaintenanceRequestResponse>?>
                ) {
                    if (response.isSuccessful){
                        val responseBody = response.body()
                        if (responseBody != null){
                            adapter = MaintenanceRequestAdapter(requests = responseBody)
                        }
                    }

                }

                override fun onFailure(
                    call: Call<List<MaintenanceRequestResponse>?>,
                    t: Throwable
                ) {
                    CustomToast.show(this@MaintenanceRequest, "${t.message}", CustomToast.Companion.ToastType.ERROR)
                }

            })


        }
    }

//
//  //  private fun setUpTempDataAndRecyclerView(){
//        val tempRequests = listOf(
//            MaintenanceRequestData(
//                id = "001",
//                title = "Leaking Tap",
//                description = "The kitchen tap is leaking heavily.",
//                priority = "High",
//                status = "Pending",
//                unit = "Apartment 3B",
//                assignedStaff = "Unassigned",
//                dateSubmitted = "01 Sep 2025",
//                followUps = 1,
//                caretakerNote = ""
//            ),
//            MaintenanceRequestData(
//                id = "002",
//                title = "Broken Window",
//                description = "The bedroom window pane is shattered.",
//                priority = "Medium",
//                status = "In Progress",
//                unit = "Apartment 5A",
//                assignedStaff = "Jane Smith",
//                dateSubmitted = "28 Aug 2025",
//                followUps = 2,
//                caretakerNote = "Temporary cover applied"
//            ),
//            MaintenanceRequestData(
//                id = "003",
//                title = "Electric Fault",
//                description = "Power socket not working in living room.",
//                priority = "Low",
//                status = "Completed",
//                unit = "Apartment 2C",
//                assignedStaff = "Mike Johnson",
//                dateSubmitted = "25 Aug 2025",
//                followUps = 0,
//                caretakerNote = "Issue resolved, no further action"
//            )
//        )
//
//        adapter = MaintenanceRequestAdapter(tempRequests)
//        binding.rvRequests.layoutManager = LinearLayoutManager(this)
//        binding.rvRequests.adapter = adapter
//    }


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
    }
}