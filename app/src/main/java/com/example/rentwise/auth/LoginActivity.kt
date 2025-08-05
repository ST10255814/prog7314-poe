package com.example.rentwise.auth

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.rentwise.R
import com.example.rentwise.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val slogan = getString(R.string.app_slogan)
        val firstHalfOfSlogan = "Smart Rentals.".length
        val color = ContextCompat.getColor(this, R.color.light_blue)

        val spannable = SpannableString(slogan)
        spannable.setSpan(
            ForegroundColorSpan(color),
            0,
            firstHalfOfSlogan,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.appSlogan.text = spannable
    }
}