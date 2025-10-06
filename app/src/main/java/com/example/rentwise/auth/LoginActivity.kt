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
import com.example.rentwise.home.HomeScreen
import com.example.rentwise.R
import com.example.rentwise.custom_toast.CustomToast
import com.example.rentwise.data_classes.GoogleRequest
import com.example.rentwise.data_classes.GoogleResponse
import com.example.rentwise.data_classes.LoginRequest
import com.example.rentwise.data_classes.LoginResponse
import com.example.rentwise.databinding.ActivityLoginBinding
import com.example.rentwise.shared_pref_config.TokenManger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Activity responsible for handling user login, including email/password and Google SSO, with UI feedback and secure token storage.
class LoginActivity : AppCompatActivity() {

    // Holds the binding instance for accessing all views in the login layout.
    private lateinit var binding: ActivityLoginBinding
    // Manages Google Sign-In client for SSO authentication.
    private lateinit var googleSignInClient: GoogleSignInClient
    // Handles secure storage and retrieval of authentication tokens and user data.
    private lateinit var tokenManger: TokenManger
    // Request code for Google Sign-In intent result handling.
    private val RC_SIGN_IN = 9001

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManger = TokenManger(applicationContext)

        setupLoginView() // Styles the app name, slogan, and register text for branding and navigation cues.
        prepareGoogleSignIn() // Configures Google Sign-In options and initializes the client.
        setListeners() // Attaches all event listeners for user interaction, input validation, and authentication triggers.
    }

    // Configures Google Sign-In with required scopes and initializes the client for SSO.
    private fun prepareGoogleSignIn(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    // Styles the app name, slogan, and register text, applying color and underline to specific portions for visual emphasis.
    private fun setupLoginView(){
        val appName = getString(R.string.app_name)
        val halfOfAppName = "Wise".length

        val slogan = getString(R.string.app_slogan)
        val smartPortionOfSlogan = "Smart".length
        val simplePortionOfSlogan = "Simple".length

        val firstHalfOfSlogan = "Smart Rentals. ".length

        val registerText = getString(R.string.register_text)
        val registerPortion = "Register".length

        val color = ContextCompat.getColor(this, R.color.darkish_blue)

        val spannableSlogan = SpannableString(slogan)

        val spannableAppName = SpannableString(appName)
        spannableAppName.setSpan(
            ForegroundColorSpan(color),
            appName.length - halfOfAppName,
            appName.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableSlogan.setSpan(
            ForegroundColorSpan(color),
            0,
            smartPortionOfSlogan,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableSlogan.setSpan(
            ForegroundColorSpan(color),
            firstHalfOfSlogan,
            firstHalfOfSlogan + simplePortionOfSlogan,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val spannableRegister = SpannableString(registerText)
        spannableRegister.setSpan(
            UnderlineSpan(),
            registerText.length - registerPortion,
            registerText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableRegister.setSpan(
            ForegroundColorSpan(color),
            registerText.length - registerPortion,
            registerText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.appName.text = spannableAppName
        binding.appSlogan.text = spannableSlogan
        binding.registerText.text = spannableRegister
    }

    @SuppressLint("ClickableViewAccessibility")
    // Sets up all listeners for UI elements, including navigation, button animations, input validation, and authentication triggers.
    private fun setListeners(){
        // Navigates to the registration screen when the register text is clicked.
        binding.registerText.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Validates user input and triggers the login API call when the login button is clicked.
        binding.loginBtn.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()

            // Checks for empty fields and valid email format before proceeding.
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() || password.isEmpty()) {
                binding.emailLayout.error = "Email can not be empty"
                binding.passwordLayout.error = "Password can not be empty"
            }
            else{
                loginAPICall(email, password) // Initiates the login process via API.
            }
        }

        // Clears the email error message as soon as the user starts typing.
        binding.edtEmail.addTextChangedListener { text ->
            if (!text.isNullOrEmpty()) {
                binding.emailLayout.error = null
            }
        }

        // Clears the password error message as soon as the user starts typing.
        binding.edtPassword.addTextChangedListener { text ->
            if (!text.isNullOrEmpty()) {
                binding.passwordLayout.error = null
            }
        }

        // Placeholder for biometric authentication trigger (to be implemented).
        binding.fingerprintAnimation.setOnClickListener {
            //authenticate()
        }

        // Adds a press animation to the login button for tactile feedback.
        binding.loginBtn.setOnTouchListener { v, event ->
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

        // Adds a press animation to the Google Sign-In button for tactile feedback.
        binding.googleSignInBtn.setOnTouchListener { v, event ->
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

        // Adds a press animation to the register text for tactile feedback.
        binding.registerText.setOnTouchListener { v, event ->
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

        // Handles Google Sign-In button click, ensuring account selection and launching the sign-in intent.
        binding.googleSignInBtn.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }

    // Handles the login API call, manages UI overlays, and processes server responses for success or error.
    private fun loginAPICall(email: String, password: String){
        showLoginOverlay() // Displays a loading overlay to prevent duplicate submissions.
        val request = LoginRequest(
            email = email,
            password = password
        )

        val api = RetrofitInstance.createAPIInstance(applicationContext)
        api.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if(response.isSuccessful) {
                    hideLoginOverlay()
                    val authResponse = response.body()
                    // Saves the JWT token and user ID in secure shared preferences for session management.
                    if(authResponse != null){
                        authResponse.token.let {
                            if (it != null) {
                                tokenManger.saveToken(it)
                            }
                        }
                        authResponse.userId.let {
                            if(it != null){
                                tokenManger.saveUser(it)
                            }
                        }
                        CustomToast.show(this@LoginActivity, "${authResponse.message}", CustomToast.Companion.ToastType.SUCCESS)
                        val intent = Intent(this@LoginActivity, HomeScreen::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
                else{
                    hideLoginOverlay()
                    // Extracts and displays error messages from the server response.
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (errorBody != null) {
                        try {
                            val json = JSONObject(errorBody)
                            json.getString("error")
                        } catch (e: Exception) {
                            e.message.toString()
                        }
                    } else {
                        "Unknown error"
                    }
                    binding.emailLayout.error = "Invalid Credentials"
                    binding.passwordLayout.error = "Invalid Credentials"
                    CustomToast.show(this@LoginActivity, errorMessage, CustomToast.Companion.ToastType.ERROR)
                    Log.e("Google Login API", errorMessage)
                }
            }
            // Handles network or unexpected failures during the login process.
            override fun onFailure(call: Call<LoginResponse>, t: Throwable){
                hideLoginOverlay()
                CustomToast.show(this@LoginActivity, "${t.message}", CustomToast.Companion.ToastType.ERROR)
                Log.e("Google Login Failure", "Error: ${t.message.toString()}")
            }
        })
    }

    // Makes the login overlay visible to block user interaction during network operations.
    private fun showLoginOverlay(){
        binding.loginOverlay.visibility = View.VISIBLE
    }

    // Hides the login overlay, restoring user interaction after network operations.
    private fun hideLoginOverlay(){
        binding.loginOverlay.visibility = View.GONE
    }

    @Suppress("DEPRECATION")
    // Handles the result from the Google Sign-In intent, extracting the ID token and sending it to the backend for authentication.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    Log.d("Google Token", idToken)
                    sendIdTokenToBackend(idToken)
                } else {
                    CustomToast.show(this@LoginActivity, "Failed to get Google ID Token", CustomToast.Companion.ToastType.ERROR)
                }
            } catch (e: ApiException) {
                Log.e("Google Token Error", "${e.statusCode}", e)
                CustomToast.show(this@LoginActivity, "Google sign-in failed", CustomToast.Companion.ToastType.ERROR)
            }
        }
    }

    // Sends the Google ID token to the backend for verification and handles the authentication response.
    private fun sendIdTokenToBackend(idToken: String) {
        showLoginOverlay()
        val request = GoogleRequest(
            idToken = idToken
        )
        val api = RetrofitInstance.createAPIInstance(applicationContext)
        api.googleMobileLogin(request).enqueue(object : Callback<GoogleResponse> {
            override fun onResponse(
                call: Call<GoogleResponse?>,
                response: Response<GoogleResponse?>
            ) {
                if(response.isSuccessful){
                    hideLoginOverlay()
                    val googleResponse = response.body()
                    // Saves user ID, JWT token, and profile image for session and personalization.
                    if(googleResponse != null){
                        googleResponse.token.let {
                            if(it != null){
                                tokenManger.saveToken(it)
                            }
                        }
                        googleResponse.user?.id.let {
                            if(it != null){
                                tokenManger.saveUser(it)
                            }
                        }
                        googleResponse.user?.pfpImage.let {
                            if(it != null){
                                tokenManger.savePfp(it)
                            }
                        }
                        Log.d("Profile Image after login", tokenManger.getPfp().toString())
                        CustomToast.show(this@LoginActivity, "Login Successful!", CustomToast.Companion.ToastType.SUCCESS)
                        val intent = Intent(this@LoginActivity, HomeScreen::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
                else{
                    hideLoginOverlay()
                    // Extracts and displays error messages from the server response.
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (errorBody != null) {
                        try {
                            val json = JSONObject(errorBody)
                            json.getString("error")
                        } catch (e: Exception) {
                            e.message.toString()
                        }
                    } else {
                        "Unknown error"
                    }
                    CustomToast.show(this@LoginActivity, errorMessage, CustomToast.Companion.ToastType.ERROR)
                }
            }

            // Handles network or unexpected failures during the Google login process.
            override fun onFailure(
                call: Call<GoogleResponse?>,
                t: Throwable
            ) {
                hideLoginOverlay()
                CustomToast.show(this@LoginActivity, "${t.message}", CustomToast.Companion.ToastType.ERROR)
                Log.e("Login", "Error: ${t.message.toString()}")
            }
        })
    }
}
