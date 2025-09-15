package com.example.rentwise.booking

import RetrofitInstance
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.MotionEvent
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.rentwise.R
import com.example.rentwise.adapters.FileAttachmentAdapter
import com.example.rentwise.auth.LoginActivity
import com.example.rentwise.data_classes.BookingResponse
import com.example.rentwise.databinding.ActivityBookingBinding
import com.example.rentwise.home.HomeScreen
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
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId.systemDefault
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ofPattern
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale

class Booking : AppCompatActivity() {
    private lateinit var binding: ActivityBookingBinding
    private val filesAttached = mutableListOf<Uri>()
    private lateinit var fileAdapter: FileAttachmentAdapter
    private val formatter: DateTimeFormatter = ofPattern("dd-MM-yyyy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupDatePickers()
        setListeners()
        bindPassedData()
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
        val propertyImage = intent.getStringExtra("property_image")

        if (propertyImage != null) {
            Log.d("Property Image URL", propertyImage)
            Glide.with(this)
                .load(propertyImage)
                .placeholder(R.drawable.ic_empty)
                .error(R.drawable.ic_empty)
                .into(binding.imageMain)
        }

        if(propertyName != null && propertyLocation != null){
            binding.propertyName.text = propertyName
            binding.propertyAddress.text = propertyLocation
        }
    }

    private fun createBookingApiCall() {
        val tokenManger = TokenManger(applicationContext)
        val userId = tokenManger.getUser() ?: return
        val listingId = intent.getStringExtra("property_id") ?: return

        // Retrieve and validate input fields to avoid empty submissions as well as remove leading/trailing white spaces
        val checkInDate = binding.editCheckin.text.toString().trim()
        val checkOutDate = binding.editCheckout.text.toString().trim()
        val numberOfGuests = binding.editGuests.text.toString().trim()
        val totalPrice = binding.textTotalPrice.text.toString().replace("R", "").replace(",", "").trim()

        if (checkInDate.isEmpty() || checkOutDate.isEmpty() || numberOfGuests.isEmpty() || totalPrice.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Prepare RequestBody instances for text fields
        val checkInBody = checkInDate.toRequestBody("text/plain".toMediaTypeOrNull())
        val checkOutBody = checkOutDate.toRequestBody("text/plain".toMediaTypeOrNull())
        val guestsBody = numberOfGuests.toRequestBody("text/plain".toMediaTypeOrNull())
        val priceBody = totalPrice.toRequestBody("text/plain".toMediaTypeOrNull())

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
        binding.btnConfirmBooking.isEnabled = false // Disable button to prevent multiple clicks
        val call = api.createBooking(userId, listingId, checkInBody, checkOutBody, guestsBody, multipartFiles, priceBody)
        call.enqueue(object : Callback<BookingResponse> {
            @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
            override fun onResponse(call: Call<BookingResponse>, response: Response<BookingResponse>) {
                binding.btnConfirmBooking.isEnabled = true // Re-enable button after response
                if (response.isSuccessful) {
                    Toast.makeText(this@Booking, response.body()?.message ?: "Booking successful", Toast.LENGTH_LONG).show()
                    // Clear the input fields and attached files
                    binding.editCheckin.text.clear()
                    binding.editCheckout.text.clear()
                    binding.editGuests.text.clear()
                    binding.textTotalPrice.text = "R0.00"
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
                binding.btnConfirmBooking.isEnabled = true // Re-enable button on failure
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

    @SuppressLint("SetTextI18n")
    private fun setupDatePickers() {
        // Prevent soft keyboard from opening
        binding.editCheckin.inputType = 0
        binding.editCheckout.inputType = 0

        binding.editCheckin.setOnClickListener {
            showCheckInPicker()
        }

        binding.editCheckout.setOnClickListener {
            showCheckOutPicker()
        }
    }
    @SuppressLint("SetTextI18n")
    private fun showCheckInPicker() {
        val today = LocalDate.now()

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                binding.editCheckin.setText(selectedDate.format(formatter))

                // Reset checkout if invalid
                val checkoutText = binding.editCheckout.text.toString()
                if (checkoutText.isNotEmpty()) {
                    val checkoutDate = LocalDate.parse(checkoutText, formatter)
                    if (ChronoUnit.DAYS.between(selectedDate, checkoutDate) < 1) {
                        binding.editCheckout.text.clear()
                    }
                }
                calculateTotalPrice()
            },
            today.year,
            today.monthValue - 1,
            today.dayOfMonth
        )
        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun showCheckOutPicker() {
        val checkInText = binding.editCheckin.text.toString()
        if (checkInText.isEmpty()) {
            Toast.makeText(this, "Please select check-in date first", Toast.LENGTH_SHORT).show()
            return
        }
        val checkInDate = LocalDate.parse(checkInText, formatter)
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                val diffDays = ChronoUnit.DAYS.between(checkInDate, selectedDate)

                if (diffDays < 1) {
                    Toast.makeText(this, "Checkout must be at least 1 day after check-in", Toast.LENGTH_SHORT).show()
                } else {
                    binding.editCheckout.setText(selectedDate.format(formatter))
                    calculateTotalPrice()
                }
            },
            checkInDate.year,
            checkInDate.monthValue - 1,
            checkInDate.dayOfMonth + 1
        )

        val minCheckoutMillis = checkInDate.plusDays(1)
            .atStartOfDay(systemDefault())
            .toInstant()
            .toEpochMilli()
        datePicker.datePicker.minDate = minCheckoutMillis
        datePicker.show()
    }

    @SuppressLint("SetTextI18n")
    private fun calculateTotalPrice() {
        val propertyPrice = intent.getStringExtra("property_price") ?: return
        val propertyPricePerNight = propertyPrice.toDoubleOrNull() ?: return

        val checkInText = binding.editCheckin.text.toString()
        val checkOutText = binding.editCheckout.text.toString()

        if (checkInText.isEmpty() || checkOutText.isEmpty()) {
            binding.textTotalPrice.text = "R0.00"
            return
        }

        val checkInDate = LocalDate.parse(checkInText, formatter)
        val checkOutDate = LocalDate.parse(checkOutText, formatter)

        val nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate).toInt()
        if (nights > 0) {
            val totalPrice = nights * propertyPricePerNight
            binding.textTotalPrice.text = "R%.2f".format(totalPrice)
        } else {
            binding.textTotalPrice.text = "R0.00"
        }
    }
}