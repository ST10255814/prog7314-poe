package com.example.rentwise.settings

import RetrofitInstance
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.rentwise.R
import com.example.rentwise.data_classes.UserSettingsResponse
import com.example.rentwise.databinding.ActivityProfileSettingsBinding
import com.example.rentwise.home.HomeScreen
import com.example.rentwise.shared_pref_config.TokenManger
import org.json.JSONObject
import retrofit2.Call
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import retrofit2.Callback
import retrofit2.Response

class ProfileSettings : AppCompatActivity() {
    private lateinit var binding: ActivityProfileSettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()
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
    }
    private fun getUserSettingsByLoggedInUserApiCall() {
        val tokenManager = TokenManger(applicationContext)
        val userId = tokenManager.getUser()

        if (userId != null) {
            val api = RetrofitInstance.createAPIInstance(applicationContext)
            api.getUserById(userId).enqueue(object : Callback<UserSettingsResponse> {
                override fun onResponse(
                    call: Call<UserSettingsResponse>,
                    response: Response<UserSettingsResponse>
                ) {
                    if (response.isSuccessful){
                        val userSettings = response.body()
                        if (userSettings != null){
                            binding.editUsername.setText(userSettings.username)
                            binding.editName.setText(userSettings.firstName)
                            binding.editSurname.setText(userSettings.surname)
                            binding.editEmail.setText(userSettings.email)
                            binding.editPhone.setText(userSettings.phone)
                            binding.editDob.setText(formatDoB(userSettings.dob))
                        }
                    }
                    else{
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
                        Toast.makeText(this@ProfileSettings, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(p0: Call<UserSettingsResponse>, t: Throwable) {
                    Toast.makeText(this@ProfileSettings, "${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Profile Settings", "Error: ${t.message.toString()}")
                }
            })
        }
    }

    private fun formatDoB(dob: String?): String {
        if (dob.isNullOrEmpty()) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())

            val date: Date? = inputFormat.parse(dob)
            if (date != null) outputFormat.format(date) else dob
        } catch (e: Exception) {
            dob ?: ""
        }
    }
}