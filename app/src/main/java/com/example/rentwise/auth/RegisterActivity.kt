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
    // Holds the binding instance for accessing all views in the registration layout.
    private lateinit var binding: ActivityRegisterBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prepareRegisterView() // Styles the app name and login text with color and underline for branding and navigation cues.
        setListeners() // Attaches all event listeners for user interaction, including input validation and navigation.
    }

    // Styles the app name and login text, applying color and underline to specific portions for visual emphasis.
    private fun prepareRegisterView() {
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
    // Sets up all listeners for UI elements, including navigation, button animations, input validation, and API call triggers.
    private fun setListeners() {
        // Navigates to the login screen when the login text is clicked.
        binding.loginText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Adds a press animation to the register button for tactile feedback.
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
        // Clears the FirstName error message as soon as the user starts typing.
        binding.regFirstName.addTextChangedListener { text ->
            if (!text.isNullOrEmpty()) binding.firstNameInputLayout.error = null
        }
        // Clears the LastName error message as soon as the user starts typing.
        binding.regSurname.addTextChangedListener { text ->
            if (!text.isNullOrEmpty()) binding.surnameInputLayout.error = null
        }
        // Clears the email error message as soon as the user starts typing.
        binding.regEmail.addTextChangedListener { text ->
            if (!text.isNullOrEmpty()) binding.emailInputLayout.error = null
        }

        // Clears the password error message as soon as the user starts typing.
        binding.regPassword.addTextChangedListener { text ->
            if (!text.isNullOrEmpty()) binding.passwordInputLayout.error = null
        }

                // Adds a press animation to the login text for tactile feedback.
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

                // Validates user input and triggers the registration API call when the register button is clicked.
                binding.regFirstName.addTextChangedListener { text ->
                    if (!text.isNullOrEmpty()) binding.firstNameInputLayout.error = null
                }
                binding.regSurname.addTextChangedListener { text ->
                    if (!text.isNullOrEmpty()) binding.surnameInputLayout.error = null
                }
                binding.regEmail.addTextChangedListener { text ->
                    if (!text.isNullOrEmpty()) binding.emailInputLayout.error = null
                }
                binding.regPassword.addTextChangedListener { text ->
                    if (!text.isNullOrEmpty()) binding.passwordInputLayout.error = null
                }
                // Validates user input and triggers the registration API call when the register button is clicked.
                binding.registerBtn.setOnClickListener {
                    val firstName = binding.regFirstName.text?.toString()?.trim()
                    val surname = binding.regSurname.text?.toString()?.trim()
                    val email = binding.regEmail.text?.toString()?.trim()
                    val password = binding.regPassword.text?.toString()?.trim()

                    // [RESET ERRORS] keep UI tidy before validating
                    binding.firstNameInputLayout.error = null
                    binding.surnameInputLayout.error = null
                    binding.emailInputLayout.error = null
                    binding.passwordInputLayout.error = null

                    // Checks for empty fields and valid email format before proceeding.
                    if (
                        firstName.isNullOrEmpty() ||
                        surname.isNullOrEmpty() ||
                        email.isNullOrEmpty() ||
                        !Patterns.EMAIL_ADDRESS.matcher(email).matches() ||
                        password.isNullOrEmpty()
                    ) {
                        if (firstName.isNullOrEmpty()) {
                            binding.firstNameInputLayout.error = "Required"
                        }
                        if (surname.isNullOrEmpty()) {
                            binding.surnameInputLayout.error = "Required"
                        }
                        if (email.isNullOrEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email)
                                .matches()
                        ) {
                            binding.emailInputLayout.error = "Invalid Credentials"
                        }
                        if (password.isNullOrEmpty()) {
                            binding.passwordInputLayout.error = "Required"
                        }
                    } else {
                        registerAPICall(
                            email = email,
                            password = password,
                            firstName = firstName,
                            surname = surname
                        )
                    }
                }

            }


    // Handles the registration API call, manages UI overlays, and processes server responses for success or error.
    private fun registerAPICall(email: String, password: String, firstName: String, surname: String) {
        showOverlay() // Displays a loading overlay to prevent duplicate submissions.

        val request = RegisterRequest(
            email = email,
            password = password,
            firstName = firstName, // [FIX] matches data class and backend
            surname = surname
        )
        val api = RetrofitInstance.createAPIInstance(applicationContext)
        api.register(request).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                hideOverlay()
                if (response.isSuccessful) {
                    val authResponse = response.body()
                    if (authResponse != null) {
                        CustomToast.show(
                            this@RegisterActivity,
                            "${authResponse.message}",
                            CustomToast.Companion.ToastType.SUCCESS
                        )
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (errorBody != null) {
                        try {
                            val json = JSONObject(errorBody)
                            json.getString("error")
                        } catch (e: Exception) {
                            e.printStackTrace()
                            "Unexpected error"
                        }
                    } else {
                        "Unknown error"
                    }

                    // set errors on the TextInputLayouts (consistent with validation UI)
                    binding.emailInputLayout.error = "Invalid Credentials"
                    binding.passwordInputLayout.error = "Invalid Credentials"

                    CustomToast.show(
                        this@RegisterActivity,
                        errorMessage,
                        CustomToast.Companion.ToastType.ERROR
                    )
                    Log.e("Register API Call", "Error: $errorMessage")
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                hideOverlay()
                CustomToast.show(
                    this@RegisterActivity,
                    "${t.message}",
                    CustomToast.Companion.ToastType.ERROR
                )
                Log.e("Register", "Error: ${t.message.toString()}")
            }
        })
    }

    // Makes the full-screen overlay visible to block user interaction during network operations.
    private fun showOverlay() {
        binding.fullScreenOverlay.visibility = View.VISIBLE
    }

    // Hides the full-screen overlay, restoring user interaction after network operations.
    private fun hideOverlay() {
        binding.fullScreenOverlay.visibility = View.GONE
    }
}

