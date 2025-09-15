package com.example.rentwise.booking

import RetrofitInstance
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentwise.adapters.FileAttachmentAdapter
import com.example.rentwise.auth.LoginActivity
import com.example.rentwise.data_classes.BookingResponse
import com.example.rentwise.databinding.ActivityBookingBinding
import com.example.rentwise.recyclerview_itemclick_views.PropertyDetails
import com.example.rentwise.shared_pref_config.TokenManger
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class Booking : AppCompatActivity() {
    private lateinit var binding: ActivityBookingBinding
    private val filesAttached = mutableListOf<Uri>()
    private lateinit var fileAdapter: FileAttachmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setListeners()
        bindPassedData()
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

        binding.btnUploadFile.setOnClickListener {
            // Code to open file picker and handle file selection
            filePickerLauncher.launch("*/*")
        }

        binding.btnConfirmBooking.setOnClickListener {
            createBookingApiCall()
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

    private fun createBookingApiCall() {
        val tokenManger = TokenManger(applicationContext)
        val userId = tokenManger.getUser() ?: return
        val listingId = intent.getStringExtra("property_id") ?: return

        // Retrieve and validate input fields
        val checkInDate = binding.editCheckin.text.toString().trim()
        val checkOutDate = binding.editCheckout.text.toString().trim()
        val numberOfGuests = binding.editGuests.text.toString().trim()

        if (checkInDate.isEmpty() || checkOutDate.isEmpty() || numberOfGuests.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare RequestBody instances for text fields
        val checkInBody = checkInDate.toRequestBody("text/plain".toMediaTypeOrNull())
        val checkOutBody = checkOutDate.toRequestBody("text/plain".toMediaTypeOrNull())
        val guestsBody = numberOfGuests.toRequestBody("text/plain".toMediaTypeOrNull())

        val multipartFiles = mutableListOf<MultipartBody.Part>() // Prepare list for MultipartBody.Part
        for (uri in filesAttached) { // Convert each URI to MultipartBody.Part
            val inputStream = contentResolver.openInputStream(uri) // Open InputStream from URI
            val bytes = inputStream!!.readBytes() // Read bytes from InputStream
            val mimeType = contentResolver.getType(uri) ?: "application/octet-stream" // Fallback MIME type if null
            // Create RequestBody and MultipartBody.Part
            val body = MultipartBody.Part.createFormData(
                "supportDocuments",
                getFileName(uri),
                RequestBody.create(mimeType.toMediaTypeOrNull(), bytes)
            )
            multipartFiles.add(body)
        }

        val api = RetrofitInstance.createAPIInstance(applicationContext)
        binding.btnConfirmBooking.isEnabled = false
        val call = api.createBooking(userId, listingId, checkInBody, checkOutBody, guestsBody, multipartFiles)
        call.enqueue(object : Callback<BookingResponse> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<BookingResponse>, response: Response<BookingResponse>) {
                binding.btnConfirmBooking.isEnabled = true
                if (response.isSuccessful) {
                    Toast.makeText(this@Booking, response.body()?.message ?: "Booking successful", Toast.LENGTH_LONG).show()
                    // Clear the input fields and attached files
                    binding.editCheckin.text.clear()
                    binding.editCheckout.text.clear()
                    binding.editGuests.text.clear()
                    filesAttached.clear()
                    fileAdapter.notifyDataSetChanged()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = errorBody ?: "Unknown error"
                    Toast.makeText(this@Booking, errorMessage, Toast.LENGTH_SHORT).show()

                    //Logout user if 401 Unauthorized
                    if(response.code() == 401) {
                        tokenManger.clearToken()
                        tokenManger.clearUser()
                        val intent = Intent(this@Booking, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }
            }
            override fun onFailure(call: Call<BookingResponse>, t: Throwable) {
                binding.btnConfirmBooking.isEnabled = true
                Toast.makeText(this@Booking, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    // Allowed MIME types for file selection
    private val allowedMimeTypes = arrayOf(
        "image/jpeg", // jpg, jpeg
        "image/png",  // png
        "application/pdf", // pdf
        "application/msword", // doc
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // docx
    )

    // File picker launcher
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        // Check if a file was selected
        if (uri != null) {
            val mimeType = contentResolver.getType(uri)
            // Validate MIME type
            if (mimeType !in allowedMimeTypes) {
                Toast.makeText(this, "Invalid file type", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            // Check for duplicates
            if (filesAttached.contains(uri)) {
                Toast.makeText(this, "File already attached", Toast.LENGTH_SHORT).show()
            } else {
                // Add new file
                filesAttached.add(uri)
                fileAdapter.notifyItemInserted(filesAttached.size - 1)
                Toast.makeText(this, "Selected file: ${getFileName(uri)}", Toast.LENGTH_SHORT).show()
            }
        }
    }



    // Get file name from URI
    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") { // Use content resolver to get file name
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path?.substringAfterLast('/') ?: "unknown_file"
        }
        return result ?: "unknown_file"
    }


    // Setup RecyclerView for displaying selected files
    private fun setupRecyclerView() {
        fileAdapter = FileAttachmentAdapter(filesAttached) { position ->
            filesAttached.removeAt(position)
            fileAdapter.notifyItemRemoved(position)
        }
        binding.rvSelectedFiles.layoutManager = LinearLayoutManager(this)
        binding.rvSelectedFiles.adapter = fileAdapter
    }
}