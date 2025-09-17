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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.example.rentwise.home.HomeScreen
import com.example.rentwise.R
import com.example.rentwise.custom_toast.CustomToast
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

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLoginView()
        setListeners()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupLoginView(){
        //Spannable text implementation by:
        //https://youtu.be/UR-oQynC12E?si=_2Lvcr7al9a4wgov
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
    private fun setListeners(){
        binding.registerText.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.loginBtn.setOnClickListener {
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()

            if(email.isNullOrEmpty() || password.isNullOrEmpty()){
                binding.emailLayout.error = "Email can not be empty"
                binding.passwordLayout.error = "Password can not be empty"
            }
            else{
                //API Call
                loginAPICall(email, password)
            }
        }

        binding.edtEmail.addTextChangedListener { text ->
            if (!text.isNullOrEmpty()) {
                binding.emailLayout.error = null
            }
        }

        binding.edtPassword.addTextChangedListener { text ->
            if (!text.isNullOrEmpty()) {
                binding.passwordLayout.error = null
            }
        }

        //Button Animation and states followed by ChatGPT
        //https://chatgpt.com/share/689214fa-941c-800a-a9d7-81bfe8fefbf1
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

        binding.googleSignInBtn.setOnClickListener {
            // Implement Google Sign in
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

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
    }

    private fun loginAPICall(email: String, password: String){
        showLoginOverlay()
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
                    if(authResponse != null){
                        val tokenManger = TokenManger(applicationContext)
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
                    binding.emailLayout.error = "Invalid Email"
                    binding.passwordLayout.error = "Invalid Password"
                    CustomToast.show(this@LoginActivity, errorMessage, CustomToast.Companion.ToastType.ERROR)
                }
            }
            override fun onFailure(call: Call<LoginResponse>, t: Throwable){
                hideLoginOverlay()
                CustomToast.show(this@LoginActivity, "${t.message}", CustomToast.Companion.ToastType.ERROR)
                Log.e("Login", "Error: ${t.message.toString()}")
            }
        })
    }

    private fun showLoginOverlay(){
        binding.loginOverlay.visibility = View.VISIBLE
    }

    private fun hideLoginOverlay(){
        binding.loginOverlay.visibility = View.GONE
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    Log.d("Google Token", idToken)
                    //sendIdTokenToBackend(idToken)
                } else {
                    CustomToast.show(this@LoginActivity, "Failed to get Google ID Token", CustomToast.Companion.ToastType.ERROR)
                }
            } catch (e: ApiException) {
                Log.e("Google Token Error", "${e.statusCode}", e)
                CustomToast.show(this@LoginActivity, "Google sign-in failed: ${e.message}", CustomToast.Companion.ToastType.ERROR)
            }
        }
    }

    private fun sendIdTokenToBackend(idToken: String) {

    }
}