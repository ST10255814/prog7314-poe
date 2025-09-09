package com.example.rentwise.booking

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentwise.adapters.FileAttachmentAdapter
import com.example.rentwise.databinding.ActivityBookingBinding
import com.example.rentwise.recyclerview_itemclick_views.PropertyDetails

class Booking : AppCompatActivity() {
    private lateinit var binding: ActivityBookingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()
        populateRecyclerView()
        bindPassedData()
    }

    private fun populateRecyclerView(){
        val fileList = mutableListOf(
            "Lease_Agreement.pdf",
            "Proof_of_Identity.jpg",
            "Utility_Bill_July_2025.png",
            "Additional_Document.docx"
        )
        binding.rvSelectedFiles.layoutManager = LinearLayoutManager(this)
        binding.rvSelectedFiles.adapter = FileAttachmentAdapter(fileList) { selectedFile ->
            fileList.removeAt(selectedFile)
            binding.rvSelectedFiles.adapter?.notifyItemRemoved(selectedFile)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners(){
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, PropertyDetails::class.java)
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
        binding.btnConfirmBooking.setOnTouchListener { v, event ->
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
        binding.btnUploadFile.setOnTouchListener { v, event ->
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

    private fun bindPassedData(){
        val propertyName = intent.getStringExtra("property_name")
        val propertyLocation = intent.getStringExtra("property_location")

        if(propertyName != null && propertyLocation != null){
            binding.propertyName.text = propertyName
            binding.propertyAddress.text = propertyLocation
        }
    }
}