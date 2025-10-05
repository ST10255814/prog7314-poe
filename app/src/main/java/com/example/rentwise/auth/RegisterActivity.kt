package com.example.rentwise.auth

import com.example.rentwise.retrofit_instance.RetrofitInstance
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.util.Patterns
import android.view.MotionEvent
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.rentwise.R
import com.example.rentwise.custom_toast.CustomToast
import com.example.rentwise.data_classes.RegisterRequest
import com.example.rentwise.data_classes.RegisterResponse
import com.example.rentwise.databinding.ActivityRegisterBinding
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    // View Binding handle for views in layout (RegisterActivity).
    private lateinit var binding: ActivityRegisterBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prepareRegisterView() // Static UI text styling (Brand + "Login" linked look)
        setListeners() // Attaches event listeners to UI elements (Clicks, presses, validation, submit)
    }

    private fun prepareRegisterView(){
        val appName = getString(R.string.app_name)
        val halfOfAppName = "Wise".length

        val loginText = getString(R.string.login_message)
        val loginPortion = "Login".length

        val color = ContextCompat.getColor(this, R.color.darkish_blue)

        val spannableAppName = SpannableString(appName)
        spannableAppName.setSpan(
            ForegroundColorSpan(color),
            appName.length - halfOfAppName,
            appName.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val spannableLoginText = SpannableString(loginText)
        spannableLoginText.setSpan(
            UnderlineSpan(),
            loginText.length - loginPortion,
            loginText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableLoginText.setSpan(
            ForegroundColorSpan(color),
            loginText.length - loginPortion,
            loginText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.appName.text = spannableAppName
        binding.loginText.text = spannableLoginText
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners(){
        binding.loginText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
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

        // Clears error messages when user starts typing
        binding.regEmail.addTextChangedListener { text ->
            if (!text.isNullOrEmpty()) {
                binding.emailInputLayout.error = null
            }
        }

        binding.regPassword.addTextChangedListener { text ->
            if (!text.isNullOrEmpty()) {
                binding.passwordInputLayout.error = null
            }
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
        // Validates inputs and calls API
        binding.registerBtn.setOnClickListener{
            val email = binding.regEmail.text.toString()
            val password = binding.regPassword.text.toString()

            // Basic validation for empty fields and email format
            if(email.isNullOrEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() || password.isNullOrEmpty()){
                binding.emailInputLayout.error = "Invalid Credentials"
                binding.passwordInputLayout.error = "Invalid Credentials"
            }
            else{
                registerAPICall(email, password) // Calls the register API endpoint
            }
        }
    }

    private fun registerAPICall(email: String, password: String){
        showOverlay() // Show overlay to prevent multiple requests
        // Prepare request body
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
                // Handle successful response
                if(response.isSuccessful){
                    hideOverlay() // Hide overlay on successful response
                    val authResponse = response.body() // Get the response body
                    if(authResponse != null){
                        // Toast message from server if successful
                        CustomToast.show(this@RegisterActivity, "${authResponse.message}", CustomToast.Companion.ToastType.SUCCESS)
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java) // Navigate to login after registration
                        startActivity(intent)
                        finish()
                    }
                }
                else{
                    hideOverlay() // Hide overlay on error response
                    // Parse error message from response body
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (errorBody != null) {
                        try {
                            val json = JSONObject(errorBody) // Parse the error body as JSON
                            json.getString("error")
                        } catch (e: Exception) { // Handle JSON parsing errors
                            e.printStackTrace()
                            "Unexpected error"
                        }
                    } else {
                        "Unknown error"
                    }
                    //Set error messages on the text fields
                    binding.regEmail.error = "Invalid Credentials"
                    binding.regPassword.error = "Invalid Credentials"
                    CustomToast.show(this@RegisterActivity, errorMessage, CustomToast.Companion.ToastType.ERROR)
                    Log.e("Register API Call", "Error: $errorMessage")
                }
            }
            override fun onFailure(call: Call<RegisterResponse>, t: Throwable){
                hideOverlay()
                CustomToast.show(this@RegisterActivity, "${t.message}", CustomToast.Companion.ToastType.ERROR)
                Log.e("Register", "Error: ${t.message.toString()}")
            }
        })
    }
    private fun showOverlay(){
        binding.fullScreenOverlay.visibility = View.VISIBLE
    }
    private fun hideOverlay(){
        binding.fullScreenOverlay.visibility = View.GONE
    }
}