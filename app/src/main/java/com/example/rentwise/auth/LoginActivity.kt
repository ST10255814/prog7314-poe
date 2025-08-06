package com.example.rentwise.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.rentwise.home.HomeScreen
import com.example.rentwise.R
import com.example.rentwise.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        binding.registerText.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.loginBtn.setOnClickListener {
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
            finish()
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
}