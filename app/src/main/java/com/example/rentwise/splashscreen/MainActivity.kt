package com.example.rentwise.splashscreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.airbnb.lottie.LottieAnimationView
import com.example.rentwise.R
import com.example.rentwise.auth.LoginActivity
import com.example.rentwise.shared_pref_config.TokenManger
import com.example.rentwise.home.HomeScreen


// Main entry point for the app, responsible for displaying the splash screen animation and transitioning to the login screen.
// Utilizes Android's splash screen API and Lottie for animated graphics, ensuring a smooth user experience during app startup.
class MainActivity : AppCompatActivity() {

    // Handler tied to the main thread, used for scheduling the transition after a delay.
    private val handler = Handler(Looper.getMainLooper())
    // Runnable that starts the LoginActivity and finishes the splash screen activity.
    private val runnable = Runnable {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    // Initializes the splash screen, sets up the animation, and schedules the transition to the login screen.
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // Installs the system splash screen for a seamless launch experience.
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main) // Sets the layout containing the Lottie animation view.

        val lottieView = findViewById<LottieAnimationView>(R.id.lottieView)
        lottieView?.playAnimation() // Starts the Lottie animation for visual engagement.

        handler.postDelayed(runnable, 4000) // Schedules the transition after 4 seconds.
    }

    // Cleans up the handler to prevent memory leaks or delayed transitions if the activity is destroyed early.
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}