package com.example.rentwise.settings

import RetrofitInstance
import android.annotation.SuppressLint
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
import com.bumptech.glide.Glide
import com.example.rentwise.R
import com.example.rentwise.auth.LoginActivity
import com.example.rentwise.custom_toast.CustomToast
import com.example.rentwise.data_classes.UpdateSettingsResponse
import com.example.rentwise.data_classes.UserSettingsResponse
import com.example.rentwise.databinding.ActivityProfileSettingsBinding
import com.example.rentwise.home.HomeScreen
import com.example.rentwise.shared_pref_config.TokenManger
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ofPattern
import java.util.Calendar

class ProfileSettings : AppCompatActivity() {
    private lateinit var binding: ActivityProfileSettingsBinding
    private lateinit var tokenManger: TokenManger
    private val settingsParts = mutableListOf<MultipartBody.Part>()
    private var selectedProfilePic: Uri? = null
    private val formatter: DateTimeFormatter = ofPattern("yyyy-MM-dd")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManger = TokenManger(applicationContext)

        setListeners()
        setupDatePickers()
        getUserSettingsByLoggedInUserApiCall()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners(){
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
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
            finish()
        }
        binding.saveButton.setOnTouchListener { v, event ->
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
        binding.saveButton.setOnClickListener {
            updateUserSettings()
        }
        binding.editProfileImage.setOnTouchListener { v, event ->
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
        binding.editProfileImage.setOnClickListener {
            filePickerLauncher.launch("image/*") //Image picker launcher
        }
    }
    private fun getUserSettingsByLoggedInUserApiCall() {
        showOverlay()
        val userId = tokenManger.getUser()

        if (userId != null) {
            val api = RetrofitInstance.createAPIInstance(applicationContext)
            api.getUserById(userId).enqueue(object : Callback<UserSettingsResponse> {
                override fun onResponse(
                    call: Call<UserSettingsResponse>,
                    response: Response<UserSettingsResponse>
                ) {
                    if (response.isSuccessful){
                        hideOverlay()
                        val userSettings = response.body()
                        if (userSettings != null){
                            //Bind the fetched user profile data
                            userSettings.profile.let {
                                if(it != null){
                                    with(binding){
                                        editUsername.setText(it.username)
                                        editFirstName.setText(it.firstName)
                                        editSurname.setText(it.surname)
                                        editEmail.text = it.email
                                        editPhone.setText(it.phone)
                                        editDob.setText(it.DoB)
                                    }
                                }
                            }
                            //Bind the users pfp if it exists if not assign a default
                            userSettings.profile?.pfpImage.let {
                                if (it != null){
                                    Glide.with(this@ProfileSettings)
                                        .load(it)
                                        .placeholder(R.drawable.profile_icon)
                                        .error(R.drawable.profile_icon)
                                        .circleCrop()
                                        .into(binding.profileImage)
                                }
                                else{
                                    Glide.with(this@ProfileSettings)
                                        .load(R.drawable.profile_icon)
                                        .placeholder(R.drawable.profile_icon)
                                        .error(R.drawable.profile_icon)
                                        .circleCrop()
                                        .into(binding.profileImage)
                                }
                            }
                        }
                    }
                    else{
                        hideOverlay()
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = if (errorBody != null) {
                            try {
                                val json = JSONObject(errorBody)
                                json.getString("error")
                            } catch (e: Exception) {
                                "Unknown error"
                            }
                        } else {
                            "Unknown error"
                        }
                        // Log out if unauthorized
                        if (response.code() == 401) {
                            tokenManger.clearToken()
                            tokenManger.clearUser()
                            tokenManger.clearPfp()

                            val intent = Intent(this@ProfileSettings, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //Clear Activity trace
                            startActivity(intent)
                        }
                        CustomToast.show(this@ProfileSettings, errorMessage, CustomToast.Companion.ToastType.ERROR)
                    }
                }

                override fun onFailure(p0: Call<UserSettingsResponse>, t: Throwable) {
                    hideOverlay()
                    CustomToast.show(this@ProfileSettings, "${t.message}", CustomToast.Companion.ToastType.ERROR)
                    Log.e("Profile Settings", "Error: ${t.message.toString()}")
                }
            })
        }
    }

    private fun updateUserSettings(){
        showUpdatingOverlay()
        val userId = tokenManger.getUser() ?: return

        //Get the user inputs
        val usernameInput = binding.editUsername.text.toString().trim()
        val firstNameInput = binding.editFirstName.text.toString().trim()
        val surnameInput = binding.editSurname.text.toString().trim()
        val phoneInput = binding.editPhone.text.toString().trim()
        val dobInput = binding.editDob.text.toString().trim()

        //Create the form data for none null data
        createPart("username", usernameInput)?.let { settingsParts.add(it) }
        createPart("firstName", firstNameInput)?.let { settingsParts.add(it) }
        createPart("surname", surnameInput)?.let { settingsParts.add(it) }
        createPart("phone", phoneInput)?.let { settingsParts.add(it) }
        createPart("DoB", dobInput)?.let { settingsParts.add(it) }

        // Convert attached image URI to MultipartBody.Part
        if(selectedProfilePic != null){
            val inputStream = contentResolver.openInputStream(selectedProfilePic!!) // Open InputStream from URI
            val bytes = inputStream!!.readBytes() // Read bytes from InputStream
            val mimeType = contentResolver.getType(selectedProfilePic!!) ?: "application/octet-stream" // Fallback MIME type if null
            // Create RequestBody and MultipartBody.Part for the pfp
            val body = MultipartBody.Part.createFormData(
                "profilePicture",
                getFileName(selectedProfilePic!!),
                RequestBody.create(mimeType.toMediaTypeOrNull(), bytes)
            )
            settingsParts.add(body) //add it to the list to be passed to the api
        }

        val api = RetrofitInstance.createAPIInstance(applicationContext) //Initialise the api instance
        api.updateUserSettings(userId, settingsParts).enqueue( object : Callback<UpdateSettingsResponse> {
            override fun onResponse(
                call: Call<UpdateSettingsResponse?>,
                response: Response<UpdateSettingsResponse?>
            ) {
                if(response.isSuccessful){
                    hideUpdatingOverlay()
                    val responseBody = response.body()
                    if(responseBody != null){
                        //Display success message
                        CustomToast.show(this@ProfileSettings, response.body()?.message ?: "Profile Updated", CustomToast.Companion.ToastType.SUCCESS)
                        settingsParts.clear()
                        responseBody.profile?.pfpImage.let {
                            if(it != null){
                                tokenManger.savePfp(it)
                            }
                        }
                    }
                }
                else{
                    hideUpdatingOverlay()
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (errorBody != null) {
                        try {
                            val json = JSONObject(errorBody)
                            json.getString("error")
                        } catch (e: Exception) {
                            "Unknown error"
                        }
                    } else {
                        "Unknown error"
                    }
                    // Log out if unauthorized
                    if (response.code() == 401) {
                        tokenManger.clearToken()
                        tokenManger.clearUser()
                        tokenManger.clearPfp()

                        val intent = Intent(this@ProfileSettings, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //Clear Activity trace
                        startActivity(intent)
                    }
                    CustomToast.show(this@ProfileSettings, errorMessage, CustomToast.Companion.ToastType.ERROR)
                }
            }

            override fun onFailure(
                call: Call<UpdateSettingsResponse?>,
                t: Throwable
            ) {
                hideUpdatingOverlay()
                CustomToast.show(this@ProfileSettings, "${t.message}", CustomToast.Companion.ToastType.ERROR)
                Log.e("Profile Settings", "Error: ${t.message.toString()}")
            }
        })
    }

    //Method to help create multi part form data
    private fun createPart(key: String, value: String?): MultipartBody.Part? {
        return value?.takeIf { it.isNotBlank() }?.let {
            val requestBody = it.toRequestBody("text/plain".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(key, null, requestBody)
        }
    }


    //Allowed MIME types
    private val allowedMimeTypes = arrayOf(
        "image/jpeg", // jpg, jpeg
        "image/png"  // png
    )

    // File picker launcher intent
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        // Check if a file was selected
        if (uri != null) {
            val mimeType = contentResolver.getType(uri)
            // Validate MIME type
            if (mimeType !in allowedMimeTypes) {
                CustomToast.show(this, "Invalid file type", CustomToast.Companion.ToastType.ERROR)
                return@registerForActivityResult
            }

            // Add new file
            selectedProfilePic = uri
            Glide.with(this)
                .load(selectedProfilePic)
                .placeholder(R.drawable.profile_icon)
                .error(R.drawable.profile_icon)
                .circleCrop()
                .into(binding.profileImage)
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

    private fun setupDatePickers() {
        binding.editDob.setOnClickListener {
            showDatePicker()
        }
    }

    //Modern DoB date picker with help from OpenAI
    //OpenAI. 2025. For this current date picker i have been using, please show me how i can make it so that this is more user friendly when selecting
    // a birth date as with the current calendar its not user friendly. Also how can i set the max date to todays date as well as set the
    // minimum selectable date?. [ChatGPT]. Available at: <https://chatgpt.com/share/68d2a237-db98-8012-adab-45028f212c1c> [Accessed 23 September 2025].
    private fun showDatePicker() {
        // Maximum selectable date (today)
        val today = MaterialDatePicker.todayInUtcMilliseconds()

        // Minimum selectable date (100 years ago)
        val hundredYearsAgo = Calendar.getInstance().apply {
            add(Calendar.YEAR, -100)
        }.timeInMillis

        val constraints = CalendarConstraints.Builder()
            .setStart(hundredYearsAgo) // min date
            .setEnd(today) // max date
            .build()

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date of Birth")
            .setTheme(R.style.RentWiseDatePickerThemeModern)
            .setCalendarConstraints(constraints)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            // selection is in UTC milliseconds
            val selectedDate = Instant.ofEpochMilli(selection)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            binding.editDob.setText(selectedDate.format(formatter))
        }

        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }
    private fun showOverlay(){
        binding.fullScreenOverlay.visibility = View.VISIBLE
    }

    private fun hideOverlay(){
        binding.fullScreenOverlay.visibility = View.GONE
    }
    private fun showUpdatingOverlay(){
        binding.updatingOverlay.visibility = View.VISIBLE
    }
    private fun hideUpdatingOverlay(){
        binding.updatingOverlay.visibility = View.GONE
    }
}