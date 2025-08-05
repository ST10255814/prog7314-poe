package com.example.rentwise

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rentwise.databinding.ActivityHomeScreenBinding

class HomeScreen : AppCompatActivity() {
    private lateinit var binding: ActivityHomeScreenBinding

    private lateinit var navButtons: List<ImageButton>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navButtons = listOf(
            binding.navHome,
            binding.navQuery,
            binding.navWishlist,
            binding.navNotification
        )

        setButtonListeners()
    }

    private fun setButtonListeners() {
        binding.navHome.setOnClickListener {
            selectButton(binding.navHome)
            Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show()
        }

        binding.navQuery.setOnClickListener {
            selectButton(binding.navQuery)
            Toast.makeText(this, "Query clicked", Toast.LENGTH_SHORT).show()
        }

        binding.navWishlist.setOnClickListener {
            selectButton(binding.navWishlist)
            Toast.makeText(this, "Wishlist clicked", Toast.LENGTH_SHORT).show()
        }

        binding.navNotification.setOnClickListener {
            selectButton(binding.navNotification)
            Toast.makeText(this, "Notification clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun selectButton(selectedButton: ImageButton) {
        navButtons.forEach { it.isSelected = false }

        selectedButton.isSelected = true
    }
}
