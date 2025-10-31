package com.example.rentwise.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.rentwise.databinding.ActivityHomeScreenBinding
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.rentwise.R
import com.example.rentwise.auth.LoginActivity
import com.example.rentwise.booking.BookingStatus
import com.example.rentwise.custom_toast.CustomToast
import com.example.rentwise.faq.FAQChatBot
import com.example.rentwise.maintenance.MaintenanceFragment
import com.example.rentwise.maintenance.MaintenanceRequest
import com.example.rentwise.notifications.NotificationsFragment
import com.example.rentwise.settings.MainSettingsFragment
import com.example.rentwise.settings.ProfileSettings
import com.example.rentwise.shared_pref_config.TokenManger
import com.example.rentwise.utils.LocaleHelper
import com.example.rentwise.wishlist.WishlistFragment

// Main activity for the home screen, managing navigation, drawer interactions, and fragment transactions.
class HomeScreen : AppCompatActivity() {
    // Binds the layout for the home screen, providing access to all UI elements.
    private lateinit var binding: ActivityHomeScreenBinding
    // Holds references to the bottom navigation buttons for easy state management.
    private lateinit var bottomNavButtons: List<ImageButton>
    // Holds references to the drawer navigation text views for selection handling.
    private lateinit var drawerNavButtons: List<TextView>

    // This ensures the saved language is applied when the activity is created
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configures the window to allow content to extend into system windows for a modern look.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Initializes the bottom navigation buttons for quick access to main sections.
        bottomNavButtons = listOf(
            binding.navHome,
            binding.navQuery,
            binding.navWishlist,
            binding.navNotification,
            binding.navMaintenance
        )

        // Initializes the drawer navigation buttons for settings and profile access.
        drawerNavButtons = listOf(
            binding.profileSettingsText,
            binding.settingsTabText
        )

        setButtonListeners() // Attaches all event listeners for navigation and interaction.
        commitFragmentToContainer(HomeFragment()) // Loads the default home fragment.
        selectNavButton(binding.navHome) // Highlights the home button as selected.
        binding.btnOpenDrawer.bringToFront() // Ensures the drawer button is always accessible.
    }

    // Replaces the current fragment in the container with the specified fragment, supporting back navigation.
    private fun commitFragmentToContainer(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    // Animates and displays the custom navigation drawer, sliding it in from the left.
    private fun openDrawer() {
        binding.customDrawer.visibility = View.VISIBLE
        binding.drawerOverlay.visibility = View.VISIBLE
        binding.customDrawer.translationX = -binding.customDrawer.width.toFloat()
        binding.customDrawer.animate()
            .translationX(0f)
            .setDuration(250)
            .start()
    }

    // Animates and hides the custom navigation drawer, sliding it out to the left and hiding the overlay.
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

    // Handles the back button press, closing the drawer if open, otherwise delegating to the default behavior.
    @Deprecated("This method has been deprecated in favor of using the\n      " +
            "{@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      " +
            "The OnBackPressedDispatcher controls how back button events are dispatched\n      " +
            "to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (binding.customDrawer.isVisible) {
            closeDrawer()
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    // Sets up all click and touch listeners for navigation, drawer actions, and button animations.
    private fun setButtonListeners() {
        // Navigates to the home fragment and updates the selected state.
        binding.navHome.setOnClickListener {
            selectNavButton(binding.navHome)
            commitFragmentToContainer(HomeFragment())
        }

        // Shows an informational toast for a feature not yet implemented.
        binding.navQuery.setOnClickListener {
            selectNavButton(binding.navQuery)
            CustomToast.show(this, "To be implemented another time", CustomToast.Companion.ToastType.INFO)
        }

        // Navigates to the maintenance fragment.
        binding.navMaintenance.setOnClickListener {
            selectNavButton(binding.navMaintenance)
            commitFragmentToContainer(MaintenanceFragment())
        }

        // Opens the FAQ chatbot activity and finishes the current activity.
        binding.faqChatbotTab.setOnClickListener {
            selectDrawerNavButton(binding.faqChatbotText)
            val intent = Intent(this, FAQChatBot::class.java)
            startActivity(intent)
            finish()
        }

        // Opens the booking status activity and finishes the current activity.
        binding.bookingStatusTab.setOnClickListener {
            selectDrawerNavButton(binding.bookingStatusTabText)
            val intent = Intent(this, BookingStatus::class.java)
            startActivity(intent)
            finish()
        }

        // Opens the maintenance request activity and finishes the current activity.
        binding.maintenanceStatusTab.setOnClickListener {
            selectDrawerNavButton(binding.maintenanceTabText)
            closeDrawer()
            val intent = Intent(this, MaintenanceRequest::class.java)
            startActivity(intent)
            finish()
        }

        // Navigates to the wishlist fragment.
        binding.navWishlist.setOnClickListener {
            selectNavButton(binding.navWishlist)
            commitFragmentToContainer(WishlistFragment())
        }

        // Navigates to the notifications fragment.
        binding.navNotification.setOnClickListener {
            selectNavButton(binding.navNotification)
            commitFragmentToContainer(NotificationsFragment())
        }

        // Opens the profile settings activity and finishes the current activity.
        binding.profileSettingsTab.setOnClickListener {
            selectDrawerNavButton(binding.profileSettingsText)
            closeDrawer()
            val intent = Intent(this, ProfileSettings::class.java)
            startActivity(intent)
            finish()
        }

        // Navigates to the main settings fragment.
        binding.settingsTab.setOnClickListener {
            selectDrawerNavButton(binding.settingsTabText)
            closeDrawer()
            commitFragmentToContainer(MainSettingsFragment())
        }

        // Handles user logout by clearing stored credentials, showing a toast, and redirecting to the login screen.
        binding.logoutTab.setOnClickListener {
            selectDrawerNavButton(binding.logoutText)
            val tokenManger = TokenManger(applicationContext)
            tokenManger.clearPfp()
            tokenManger.clearToken()
            tokenManger.clearUser()
            Log.d("JWT-Token After Logout", tokenManger.getToken().toString())
            CustomToast.show(this, "Successfully logged out", CustomToast.Companion.ToastType.SUCCESS)
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Opens the custom drawer when the drawer button is clicked.
        binding.btnOpenDrawer.setOnClickListener {
            openDrawer()
        }

        // Closes the custom drawer when the overlay is clicked.
        binding.drawerOverlay.setOnClickListener {
            closeDrawer()
        }

        // Adds touch animations for visual feedback on various drawer and navigation buttons.
        binding.profileSettingsTab.setOnTouchListener { v, event ->
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

        binding.faqChatbotTab.setOnTouchListener { v, event ->
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
        binding.bookingStatusTab.setOnTouchListener { v, event ->
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
        binding.settingsTab.setOnTouchListener { v, event ->
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
        binding.logoutTab.setOnTouchListener { v, event ->
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
        binding.btnOpenDrawer.setOnTouchListener { v, event ->
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

    // Updates the selected state of bottom navigation buttons, animating the selected one for emphasis.
    private fun selectNavButton(selectedButton: ImageButton) {
        bottomNavButtons.forEach {
            it.isSelected = false
            it.animate().scaleX(1f).scaleY(1f).setDuration(150).start()
        }

        drawerNavButtons.forEach { it.isSelected = false }

        selectedButton.isSelected = true
        selectedButton.animate().scaleX(1.4f).scaleY(1.4f).setDuration(150).start()
    }

    // Updates the selected state of drawer navigation text views, ensuring only one is highlighted at a time.
    private fun selectDrawerNavButton(selectedText: TextView) {
        drawerNavButtons.forEach { it.isSelected = false }
        bottomNavButtons.forEach { it.isSelected = false }
        selectedText.isSelected = true
    }
}
