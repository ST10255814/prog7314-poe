package com.example.rentwise

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rentwise.databinding.ActivityHomeScreenBinding
import androidx.core.view.isVisible
import com.example.rentwise.auth.LoginActivity

class HomeScreen : AppCompatActivity() {
    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var bottomNavButtons: List<ImageButton>
    private lateinit var drawerNavButtons: List<TextView>

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavButtons = listOf(
            binding.navHome,
            binding.navQuery,
            binding.navWishlist,
            binding.navNotification
        )

        drawerNavButtons = listOf(
            binding.menuItem1Text,
            binding.menuItem2Text
        )

        setButtonListeners()
    }

    private fun openDrawer() {
        binding.customDrawer.visibility = View.VISIBLE
        binding.drawerOverlay.visibility = View.VISIBLE
        binding.customDrawer.translationX = -binding.customDrawer.width.toFloat()
        binding.customDrawer.animate()
            .translationX(0f)
            .setDuration(250)
            .start()
    }

    private fun closeDrawer() {
        binding.customDrawer.animate()
            .translationX(-binding.customDrawer.width.toFloat())
            .setDuration(250)
            .withEndAction {
                binding.customDrawer.visibility = View.GONE
                binding.drawerOverlay.visibility = View.GONE
            }
            .start()
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (binding.customDrawer.isVisible) {
            closeDrawer()
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setButtonListeners() {
        binding.navHome.setOnClickListener {
            selectNavButton(binding.navHome)
        }

        binding.navQuery.setOnClickListener {
            selectNavButton(binding.navQuery)
        }

        binding.navWishlist.setOnClickListener {
            selectNavButton(binding.navWishlist)
        }

        binding.navNotification.setOnClickListener {
            selectNavButton(binding.navNotification)
        }
        binding.menuItem1.setOnClickListener {
            selectDrawerNavButton(binding.menuItem1Text)
            closeDrawer()
        }
        binding.menuItem2.setOnClickListener {
            selectDrawerNavButton(binding.menuItem2Text)
            closeDrawer()
        }
        binding.menuItem3.setOnClickListener {
            selectDrawerNavButton(binding.menuItem3Text)
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        //Opens custom drawer when clicked on
        binding.btnOpenDrawer.setOnClickListener {
            openDrawer()
        }
        // Close drawer when overlay is clicked
        binding.drawerOverlay.setOnClickListener {
            closeDrawer()
        }
        binding.menuItem1.setOnTouchListener { v, event ->
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
        binding.menuItem2.setOnTouchListener { v, event ->
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
        binding.menuItem3.setOnTouchListener { v, event ->
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

    private fun selectNavButton(selectedButton: ImageButton) {
        bottomNavButtons.forEach {
            it.isSelected = false
            it.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
        }

        drawerNavButtons.forEach { it.isSelected = false }

        selectedButton.isSelected = true
        selectedButton.animate().scaleX(1.4f).scaleY(1.4f).setDuration(150).start()
    }


    private fun selectDrawerNavButton(selectedText: TextView) {
        drawerNavButtons.forEach { it.isSelected = false }

        bottomNavButtons.forEach { it.isSelected = false }

        selectedText.isSelected = true
    }
}
