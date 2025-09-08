package com.example.rentwise.auth

import RetrofitInstance
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.health.connect.datatypes.units.Length
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
import com.example.rentwise.home.HomeScreen
import com.example.rentwise.R
import com.example.rentwise.data_classes.LoginRequest
import com.example.rentwise.data_classes.LoginResponse
import com.example.rentwise.databinding.ActivityLoginBinding
import com.example.rentwise.shared_pref_config.TokenManger
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLoginView()
        setListeners()
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

        val color = ContextCompat.getColor(this, R.color.light_blue)

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

        //Blurred View tutorial followed by:
        //https://youtu.be/VEhJd1VdTcQ?si=U_C95f_xu41FSuuy
        val decorView: View = window.decorView
        val viewGroup: ViewGroup = decorView.findViewById(android.R.id.content)
        val windowBg: Drawable = decorView.background

        binding.blurView.setupWith(viewGroup)
            .setFrameClearDrawable(windowBg)
            .setBlurRadius(23f)
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
            //API Call
            loginAPICall(email, password)
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
                    val authResponse = response.body()
                    if(authResponse != null){
                        authResponse.token.let {
                            val tokenManger = TokenManger(applicationContext)
                            if (it != null) {
                                tokenManger.saveToken(it)
                            }
                        }
                        Toast.makeText(this@LoginActivity, "${authResponse.message}", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@LoginActivity, HomeScreen::class.java)
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
                            "Unknown error"
                        }
                    } else {
                        "Unknown error"
                    }
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<LoginResponse>, t: Throwable){
                Toast.makeText(this@LoginActivity, "${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("Login", "Error: ${t.message.toString()}")
            }
        })
    }
}