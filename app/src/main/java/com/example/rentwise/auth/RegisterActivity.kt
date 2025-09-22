package com.example.rentwise.auth

import RetrofitInstance
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
    private lateinit var binding: ActivityRegisterBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prepareRegisterView()
        setListeners()
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

        binding.registerBtn.setOnClickListener{
            val email = binding.regEmail.text.toString()
            val password = binding.regPassword.text.toString()

            if(email.isNullOrEmpty() || Patterns.EMAIL_ADDRESS.matcher(email).matches() || password.isNullOrEmpty()){
                binding.emailInputLayout.error = "Email can not be empty"
                binding.passwordInputLayout.error = "Password can not be empty"
            }
            else{
                registerAPICall(email, password)
            }
        }
    }

    private fun registerAPICall(email: String, password: String){
        showOverlay()
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
                    hideOverlay()
                    val authResponse = response.body()
                    if(authResponse != null){
                        CustomToast.show(this@RegisterActivity, "${authResponse.message}", CustomToast.Companion.ToastType.SUCCESS)
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
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
                            "Unexpected error"
                        }
                    } else {
                        "Unknown error"
                    }
                    CustomToast.show(this@RegisterActivity, errorMessage, CustomToast.Companion.ToastType.ERROR)
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