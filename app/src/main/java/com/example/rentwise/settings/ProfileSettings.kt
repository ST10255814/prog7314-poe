package com.example.rentwise.settings

import RetrofitInstance
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.rentwise.R
import com.example.rentwise.auth.LoginActivity
import com.example.rentwise.custom_toast.CustomToast
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
        showOverlay()
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
                        hideOverlay()
                        val userSettings = response.body()
                        if (userSettings != null){
                            with(binding){
                                editUsername.setText(userSettings.profile?.username)
                                editFirstName.setText(userSettings.profile?.firstName)
                                editSurname.setText(userSettings.profile?.surname)
                                editEmail.setText(userSettings.profile?.email)
                                editPhone.setText(userSettings.profile?.phone)
                                editDob.setText(userSettings.profile?.DoB)
                            }

                            Glide.with(this@ProfileSettings)
                                .load(userSettings.profile?.pfpImage)
                                .placeholder(R.drawable.profile_icon)
                                .error(R.drawable.ic_error)
                                .circleCrop()
                                .into(binding.profileImage)
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
                            tokenManager.clearToken()
                            tokenManager.clearUser()

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

    private fun showOverlay(){
        binding.fullScreenOverlay.visibility = View.VISIBLE
    }

    private fun hideOverlay(){
        binding.fullScreenOverlay.visibility = View.GONE
    }
}