package com.example.rentwise.recyclerview_itemclick_views

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.rentwise.R
import com.example.rentwise.booking.Booking
import com.example.rentwise.data_classes.ListingResponse
import com.example.rentwise.databinding.ActivityPropertyDetailsBinding
import com.example.rentwise.home.HomeScreen

class PropertyDetails : AppCompatActivity() {
    private lateinit var binding: ActivityPropertyDetailsBinding
    private var isFavorited = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPropertyDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Glide.with(this)
            .load(R.drawable.temp_profile)
            .circleCrop()
            .into(binding.estateAgent)

        bindPassedData()
        setButtonListeners()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setButtonListeners(){
        binding.buttonBookNow.setOnTouchListener { v, event ->
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

        binding.btnBack.setOnTouchListener { v, event ->
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
        binding.callButton.setOnTouchListener { v, event ->
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
        binding.chatButton.setOnTouchListener { v, event ->
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
        binding.favouriteBtn.setOnClickListener {
            isFavorited = !isFavorited
            updateFavouriteIcon()

            // TODO: call API to update DB here

            val scaleUp = ObjectAnimator.ofPropertyValuesHolder(
                binding.favouriteBtn,
                PropertyValuesHolder.ofFloat("scaleX", 1.2f),
                PropertyValuesHolder.ofFloat("scaleY", 1.2f)
            ).apply {
                duration = 150
                interpolator = OvershootInterpolator()
                repeatCount = 1
                repeatMode = ObjectAnimator.REVERSE
            }

            scaleUp.start()
        }
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
            finish()
        }
        binding.buttonBookNow.setOnClickListener {
            val intent = Intent(this, Booking::class.java)
            intent.putExtra("property_name", binding.titleText.text)
            intent.putExtra("property_location", binding.locationText.text)
            startActivity(intent)
            finish()
        }
        binding.ratingBar.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1.3f).scaleY(1.3f).setDuration(100).start()
                }
            }
            false
        }
        binding.submitReviewButton.setOnTouchListener { v, event ->
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

    @SuppressLint("SetTextI18n")
    private fun bindPassedData(){
        val property = intent.getSerializableExtra("property") as? ListingResponse

        property?.let { prop ->
            binding.titleText.text = prop.title
            binding.locationText.text = prop.address
            binding.priceText.text = "R${prop.price}"
            binding.propertyDescription.text = prop.description

            val images = prop.imagesURL ?: emptyList()

            if (images.isNotEmpty()) {
                Glide.with(this)
                    .load(images[0])
                    .placeholder(R.drawable.ic_empty)
                    .error(R.drawable.ic_empty)
                    .into(binding.imageMain)
            }

            val extraPhotos = listOf(binding.image2, binding.image3, binding.image4)

            images.drop(1).take(3).forEachIndexed { index, imageUrl ->
                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_empty)
                    .error(R.drawable.ic_empty)
                    .into(extraPhotos[index])
            }

            for (i in images.drop(1).size until 3) {
                extraPhotos[i].visibility = View.GONE
            }

            Log.d("PropertyDetails", "DB favourite value: ${prop.isFavourite}")
            isFavorited = prop.isFavourite ?: false
            Log.d("PropertyDetails", "DB favourite value: ${prop.isFavourite}")
            updateFavouriteIcon()

            val amenityIcons = mapOf(
                "TV" to R.drawable.tv_icon,
                "Wi-Fi" to R.drawable.wifi_icon,
                "Bed" to R.drawable.bed_icon,
            )

            val amenities = prop.amenities ?: emptyList()
            val amenitiesContainer = binding.amenitiesContainer
            amenitiesContainer.removeAllViews()

            val inflater = LayoutInflater.from(this)

            for (amenity in amenities) {
                val itemView = inflater.inflate(R.layout.amenity_item, amenitiesContainer, false)

                val iconView = itemView.findViewById<ImageView>(R.id.amenityIcon)
                val textView = itemView.findViewById<TextView>(R.id.amenityText)

                textView.text = amenity

                val iconRes = amenityIcons[amenity] ?: R.drawable.ic_empty
                iconView.setImageResource(iconRes)

                amenitiesContainer.addView(itemView)
            }

            val landlord = prop.landlordInfo

            if(landlord != null){
                binding.landlordTxt.text = landlord.firstName + " " + landlord.surname
            }
        }
    }
    private fun updateFavouriteIcon() {
        if (isFavorited) {
            binding.favouriteBtn.setImageResource(R.drawable.favourite_icon_filled)
            binding.favouriteBtn.setColorFilter(ContextCompat.getColor(this, R.color.red))
        } else {
            binding.favouriteBtn.setImageResource(R.drawable.favourite_icon)
            binding.favouriteBtn.setColorFilter(ContextCompat.getColor(this, R.color.grey))
        }
    }
}