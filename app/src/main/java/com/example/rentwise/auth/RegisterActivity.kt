package com.example.rentwise.auth

import RetrofitInstance
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.rentwise.R
import com.example.rentwise.data_classes.LoginResponse
import com.example.rentwise.data_classes.RegisterRequest
import com.example.rentwise.data_classes.RegisterResponse
import com.example.rentwise.databinding.ActivityRegisterBinding
import com.example.rentwise.home.HomeScreen
import com.example.rentwise.shared_pref_config.TokenManger
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val appName = getString(R.string.app_name)
        val halfOfAppName = "Wise".length

        val loginText = getString(R.string.login_message)
        val loginPortion = "Login".length

        val color = ContextCompat.getColor(this, R.color.light_blue)

        val spannableAppName = SpannableString(appName)
        spannableAppName.setSpan(
            ForegroundColorSpan(color),
            appName.length - halfOfAppName,
            appName.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val spannableLoginText = SpannableString(loginText)
        spannableLoginText.setSpan(
            ForegroundColorSpan(color),
            loginText.length - loginPortion,
            loginText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.appName.text = spannableAppName
        binding.loginText.text = spannableLoginText

        val decorView: View = window.decorView
        val viewGroup: ViewGroup = decorView.findViewById(android.R.id.content)
        val windowBg: Drawable = decorView.background

        binding.blurView.setupWith(viewGroup)
            .setFrameClearDrawable(windowBg)
            .setBlurRadius(23f)

        binding.loginText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.registerBtn.setOnTouchListener { v, event ->
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

        binding.loginText.setOnTouchListener { v, event ->
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

        binding.registerBtn.setOnClickListener{
            val email = binding.regEmail.text.toString()
            val password = binding.regPassword.text.toString()

            RegisterAPICall(email, password)
        }

    }

    private fun RegisterAPICall(email: String, password: String){
        val request = RegisterRequest(
            email = email,
            password = password
        )

       val api = RetrofitInstance.createAPIInstance(applicationContext)
        api.register(request).enqueue(object : Callback<RegisterResponse>{
            override fun onResponse(
                call : Call<RegisterResponse>,
                response : Response<RegisterResponse>
            ){
                if(response.isSuccessful){
                    val authResponse = response.body()
                    if(authResponse != null){
                        Toast.makeText(this@RegisterActivity, "${authResponse.message}", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
                else{
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (errorBody != null) {
                        try {
                            val json = JSONObject(errorBody)
                            json.getString("error")
                        } catch (e: Exception) {
                            "Unexpected error"
                        }
                    } else {
                        "Unknown error"
                    }
                    Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<RegisterResponse>, t: Throwable){
                Toast.makeText(this@RegisterActivity, "${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("Register", "Error: ${t.message.toString()}")
            }
        })
    }
}