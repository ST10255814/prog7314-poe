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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.rentwise.R
import com.example.rentwise.databinding.ActivityRegisterBinding

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
    }
}