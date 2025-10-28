package com.example.rentwise.booking

import com.example.rentwise.retrofit_instance.RetrofitInstance
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.rentwise.R
import com.example.rentwise.adapters.FileAttachmentAdapter
import com.example.rentwise.auth.LoginActivity
import com.example.rentwise.custom_toast.CustomToast
import com.example.rentwise.data_classes.BookingResponse
import com.example.rentwise.data_classes.ListingResponse
import com.example.rentwise.databinding.ActivityBookingBinding
import com.example.rentwise.home.HomeScreen
import com.example.rentwise.shared_pref_config.TokenManger
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.ZoneId.systemDefault
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ofPattern
import java.time.temporal.ChronoUnit

// Activity for handling the property booking process, including file attachments, date selection, and API integration.
class Booking : AppCompatActivity() {
    // Binds the layout views for the booking screen.
    private lateinit var binding: ActivityBookingBinding
    // Stores URIs of files attached by the user for booking support documents.
    private val filesAttached = mutableListOf<Uri>()
    // Adapter for displaying attached files in a RecyclerView.
    private lateinit var fileAdapter: FileAttachmentAdapter
    // Formatter for displaying and parsing dates in the required format.
    private val formatter: DateTimeFormatter = ofPattern("dd-MM-yyyy")
    // Holds the price per night for the selected property.
    private var propertyPrice: Float? = null
    // Manages secure storage and retrieval of user authentication tokens.
    private lateinit var tokenManger: TokenManger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBookingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManger = TokenManger(applicationContext)

        getPropertyDetails() // Fetches and displays property details for the booking.
        setupRecyclerView() // Initializes the RecyclerView for file attachments.
        setupDatePickers() // Prepares date pickers for check-in and check-out selection.
        setListeners() // Attaches all event listeners for user interaction.
    }

    @SuppressLint("ClickableViewAccessibility")
    // Sets up listeners for navigation, file uploads, and booking confirmation, including button animations.
    private fun setListeners(){
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
            finish()
        }
        binding.btnBack.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
            false
        }
        binding.btnUploadFile.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
            false
        }

        binding.btnUploadFile.setOnClickListener {
            filePickerLauncher.launch("*/*") // Launches the file picker for user to select a file.
        }

        binding.btnConfirmBooking.setOnClickListener { v ->
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(50).withEndAction {
                v.animate().scaleX(1f).scaleY(1f).setDuration(50).start()
            }.start()
            createBookingApiCall() // Initiates the booking API call after animation.
        }
    }

    // Handles the booking API call, prepares multipart data, validates input, and processes server responses.
    private fun createBookingApiCall() {
        showBookingProcessOverlay()
        val userId = tokenManger.getUser() ?: return
        val listingId = intent.getStringExtra("propertyId") ?: return

        val checkInDate = binding.editCheckin.text.toString().trim()
        val checkOutDate = binding.editCheckout.text.toString().trim()
        val numberOfGuests = binding.editGuests.text.toString().trim()
        val totalPrice = binding.textTotalPrice.text.toString().replace("R", "").replace(",", "").trim()

        if (checkInDate.isEmpty() || checkOutDate.isEmpty() || numberOfGuests.isEmpty() || totalPrice.isEmpty()) {
            hideBookingProcessOverlay()
            CustomToast.show(this, "Please fill all fields", CustomToast.Companion.ToastType.ERROR)
            return
        }

        val checkInBody = checkInDate.toRequestBody("text/plain".toMediaTypeOrNull())
        val checkOutBody = checkOutDate.toRequestBody("text/plain".toMediaTypeOrNull())
        val guestsBody = numberOfGuests.toRequestBody("text/plain".toMediaTypeOrNull())
        val priceBody = totalPrice.toRequestBody("text/plain".toMediaTypeOrNull())

        val multipartFiles = mutableListOf<MultipartBody.Part>()
        for (uri in filesAttached) {
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream!!.readBytes()
            val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
            val body = MultipartBody.Part.createFormData(
                "supportDocuments",
                getFileName(uri),
                RequestBody.create(mimeType.toMediaTypeOrNull(), bytes)
            )
            multipartFiles.add(body)
        }

        if (multipartFiles.isEmpty()){
            hideBookingProcessOverlay()
            CustomToast.show(this, "Please attach at least one file", CustomToast.Companion.ToastType.ERROR)
            return
        }

        val api = RetrofitInstance.createAPIInstance(applicationContext)
        api.createBooking(userId, listingId, checkInBody, checkOutBody, guestsBody, multipartFiles, priceBody).enqueue(object : Callback<BookingResponse> {
            @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
            override fun onResponse(call: Call<BookingResponse>, response: Response<BookingResponse>) {
                if (response.isSuccessful) {
                    hideBookingProcessOverlay()
                    CustomToast.show(this@Booking, response.body()?.message ?: "Booking successful", CustomToast.Companion.ToastType.SUCCESS)

                    // Captures values needed for payment processing, before clearing the form.
                    val propertyName = binding.propertyName.text.toString().orEmpty()
                    val amountRand = binding.textTotalPrice.text?.toString()?.replace("R","")?.replace(",","")!!.trim()
                    val checkIn = binding.editCheckin.text?.toString().orEmpty()
                    val checkOut = binding.editCheckout.text?.toString().orEmpty()

                    //Clear UI
                    binding.editCheckin.text.clear()
                    binding.editCheckout.text.clear()
                    binding.editGuests.text.clear()
                    binding.textTotalPrice.text = "R0.00"
                    filesAttached.clear()
                    fileAdapter.notifyDataSetChanged()

                    // Go to Booking Status (user pays only once Approved).
                    val statusIntent = Intent(this@Booking, BookingStatus::class.java)
                    statusIntent.putExtra("amount", amountRand)
                    statusIntent.putExtra("propertyName", propertyName)
                    statusIntent.putExtra("checkIn", checkIn)
                    statusIntent.putExtra("checkOut", checkOut)
                    statusIntent.putExtra("listingId", listingId)
                    startActivity(statusIntent)


                } else {
                    hideBookingProcessOverlay()
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = errorBody ?: "Unknown error"
                    CustomToast.show(this@Booking, errorMessage, CustomToast.Companion.ToastType.ERROR)
                    if(response.code() == 401) {
                        tokenManger.clearToken()
                        tokenManger.clearUser()
                        tokenManger.clearPfp()
                        val intent = Intent(this@Booking, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }
            }
            override fun onFailure(call: Call<BookingResponse>, t: Throwable) {
                hideBookingProcessOverlay()
                Log.e("Failure", "API call failed: ${t.message}" )
                CustomToast.show(this@Booking, "Error: ${t.message}", CustomToast.Companion.ToastType.ERROR)
            }
        })
    }

    // Fetches property details from the API and binds them to the UI, handling errors and authentication.
    private fun getPropertyDetails()  {
        showLoadingOverlay()
        val api = RetrofitInstance.createAPIInstance(applicationContext)
        val listingId = intent.getStringExtra("propertyId")
        if(listingId != null){
            api.getListingById(listingId).enqueue( object : Callback<ListingResponse> {
                override fun onResponse(call: Call<ListingResponse>, response: Response<ListingResponse>) {
                    if(response.isSuccessful){
                        hideLoadingOverlay()
                        val property = response.body()
                        binding.propertyName.text = property?.title ?: "N/A"
                        binding.propertyAddress.text = property?.address ?: "N/A"
                        val imageUrl = property?.imagesURL?.firstOrNull()
                        if (imageUrl != null) {
                            Glide.with(this@Booking)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_empty)
                                .error(R.drawable.ic_empty)
                                .into(binding.imageMain)
                        }
                        propertyPrice = property?.price
                    } else {
                        hideLoadingOverlay()
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = errorBody ?: "Unknown error"
                        CustomToast.show(this@Booking, errorMessage, CustomToast.Companion.ToastType.ERROR)
                        if(response.code() == 401) {
                            tokenManger.clearToken()
                            tokenManger.clearUser()
                            tokenManger.clearPfp()
                            val intent = Intent(this@Booking, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }
                    }
                }
                override fun onFailure(call: Call<ListingResponse>, t: Throwable) {
                    hideLoadingOverlay()
                    Log.e("Failure", "API call failed: ${t.message}" )
                    CustomToast.show(this@Booking, "Error: ${t.message}", CustomToast.Companion.ToastType.ERROR)
                }
            })
        }
    }

    // List of allowed MIME types for file selection to ensure only supported files are attached.
    private val allowedMimeTypes = arrayOf(
        "image/jpeg",
        "image/png",
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )

    // Handles file selection from the file picker, validates type and duplicates, and updates the RecyclerView.
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val mimeType = contentResolver.getType(uri)
            if (mimeType !in allowedMimeTypes) {
                CustomToast.show(this, "Invalid file type", CustomToast.Companion.ToastType.ERROR)
                return@registerForActivityResult
            }
            if (filesAttached.contains(uri)) {
                CustomToast.show(this, "File already attached", CustomToast.Companion.ToastType.ERROR)
            } else {
                filesAttached.add(uri)
                fileAdapter.notifyItemInserted(filesAttached.size - 1)
                CustomToast.show(this, "Selected file: ${getFileName(uri)}", CustomToast.Companion.ToastType.INFO)
            }
        }
    }

    // Retrieves the display name of a file from its URI for user feedback and multipart upload.
    //Ryudith Tutorial. 2023. Get File Real Name, File Reference, And File MimeType From Android File Picker. [video online].
    //Available at: <https://youtu.be/OFzrYr_vVWg?si=HDnikNtN69R5TE_B> [Accessed 15 September 2025].
    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
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

    // Initializes the RecyclerView for displaying attached files and handles file removal.
    private fun setupRecyclerView() {
        fileAdapter = FileAttachmentAdapter(filesAttached) { position ->
            filesAttached.removeAt(position)
            fileAdapter.notifyItemRemoved(position)
        }
        binding.rvSelectedFiles.layoutManager = LinearLayoutManager(this)
        binding.rvSelectedFiles.adapter = fileAdapter
    }

    @SuppressLint("SetTextI18n")
    // Prepares date pickers for check-in and check-out fields, ensuring correct order and format.
    private fun setupDatePickers() {
        binding.editCheckin.setOnClickListener {
            showCheckInPicker()
        }
        binding.editCheckout.setOnClickListener {
            showCheckOutPicker()
        }
    }

    @SuppressLint("SetTextI18n")
    // Displays a date picker dialog for selecting the check-in date, restricting to today or later.
    //Coding with Dev. 2023. android date picker dialog example | DatePickerDialog - Android Studio Tutorial | Kotlin. [video online]
    //Available at: <https://youtu.be/DpL8DhCNKdE?si=QkImhVsJSf1F9lTu> [Accessed 15 September 2025].
    private fun showCheckInPicker() {
        val today = LocalDate.now()
        val datePicker = DatePickerDialog(
            this,
            R.style.RentWiseDatePickerTheme,
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                binding.editCheckin.setText(selectedDate.format(formatter))
            },
            today.year,
            today.monthValue - 1,
            today.dayOfMonth
        )
        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    // Displays a date picker dialog for selecting the check-out date, ensuring it is after check-in.
    private fun showCheckOutPicker() {
        val checkInText = binding.editCheckin.text.toString()
        if (checkInText.isEmpty()) {
            CustomToast.show(this, "Please select check-in date first", CustomToast.Companion.ToastType.ERROR)
            return
        }
        val checkInDate = LocalDate.parse(checkInText, formatter)
        val datePicker = DatePickerDialog(
            this,
            R.style.RentWiseDatePickerTheme,
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                val diffDays = ChronoUnit.DAYS.between(checkInDate, selectedDate)
                if (diffDays < 1) {
                    CustomToast.show(this, "Checkout must be at least 1 day after check-in", CustomToast.Companion.ToastType.ERROR)
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
    // Calculates the total booking price based on the number of nights and property price per night.
    private fun calculateTotalPrice() {
        val propertyPricePerNight = propertyPrice ?: return
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

    // Shows a loading overlay to block user interaction during property detail fetch.
    private fun showLoadingOverlay(){
        binding.overlayLoading.visibility = View.VISIBLE
    }
    // Hides the loading overlay after property detail fetch is complete.
    private fun hideLoadingOverlay(){
        binding.overlayLoading.visibility = View.GONE
    }
    // Shows an overlay during the booking process to prevent duplicate submissions.
    private fun showBookingProcessOverlay(){
        binding.createBookingOverlay.visibility = View.VISIBLE
    }
    // Hides the booking process overlay after completion or error.
    private fun hideBookingProcessOverlay() {
        binding.createBookingOverlay.visibility = View.GONE
    }
}
