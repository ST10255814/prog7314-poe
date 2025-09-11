package com.example.rentwise.recyclerview_itemclick_views

import RetrofitInstance
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
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.rentwise.R
import com.example.rentwise.auth.LoginActivity
import com.example.rentwise.booking.Booking
import com.example.rentwise.data_classes.FavouriteListingPostResponse
import com.example.rentwise.data_classes.FavouriteListingsResponse
import com.example.rentwise.data_classes.ListingResponse
import com.example.rentwise.databinding.ActivityPropertyDetailsBinding
import com.example.rentwise.home.HomeScreen
import com.example.rentwise.shared_pref_config.TokenManger
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PropertyDetails : AppCompatActivity() {
    private lateinit var binding: ActivityPropertyDetailsBinding
    private var isFavourite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPropertyDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Glide.with(this)
            .load(R.drawable.temp_profile)
            .circleCrop()
            .into(binding.estateAgent)

        val listingId = compareWhichDataToBind()
        setButtonListeners(listingId)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setButtonListeners(listingId: String){
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
            val tokenManger = TokenManger(applicationContext)
            val userId = tokenManger.getUser()
            if(userId != null){
                val api = RetrofitInstance.createAPIInstance(applicationContext)
                api.favouriteListing(userId, listingId).enqueue( object: Callback<FavouriteListingPostResponse>{
                    override fun onResponse(
                        call: Call<FavouriteListingPostResponse>,
                        response: Response<FavouriteListingPostResponse>
                    ) {
                        if(response.isSuccessful){
                            val apiResponse = response.body()
                            if (apiResponse != null){
                                Toast.makeText(this@PropertyDetails, apiResponse.message, Toast.LENGTH_SHORT).show()
                                isFavourite = !isFavourite
                                updateFavouriteIcon(isFavourite)
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
                        }
                        else{
                            val errorBody = response.errorBody()?.string()
                            val errorMessage = if (errorBody != null) {
                                try {
                                    val json = JSONObject(errorBody)
                                    when {
                                        json.has("message") -> json.getString("message")
                                        json.has("error") -> json.getString("error")
                                        else -> "Unknown error"
                                    }
                                } catch (e: Exception) {
                                    "Unknown error"
                                }
                            } else {
                                "Unknown error"
                            }
                            Toast.makeText(this@PropertyDetails, errorMessage, Toast.LENGTH_SHORT).show()
                            Log.e("Error", errorMessage)
                            // Log out if unauthorized
                            if (response.code() == 401) {
                                tokenManger.clearToken()
                                tokenManger.clearUser()

                                val intent = Intent(this@PropertyDetails, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //Clear Activity trace
                                startActivity(intent)
                            }
                        }
                    }

                    override fun onFailure(call: Call<FavouriteListingPostResponse>, t: Throwable) {
                        Toast.makeText(this@PropertyDetails, "${t.message}", Toast.LENGTH_SHORT).show()
                        Log.e("Error", t.message.toString())
                    }
                })
            }
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

    private fun compareWhichDataToBind() : String {
        val property = intent.getSerializableExtra("property") as? ListingResponse

        val listingId: String = if(property != null){
            bindPassedDataFromHomeFragment()
        } else{
            bindDataPassedFromWishlistFragment()
        }

        return listingId
    }

    @SuppressLint("SetTextI18n")
    private fun bindPassedDataFromHomeFragment() : String {
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

            isFavourite = prop.isFavourite ?: false
            updateFavouriteIcon(isFavourite)

            val amenityIcons = mapOf(
                "tv" to R.drawable.tv_icon,
                "wi-fi" to R.drawable.wifi_icon,
                "bed" to R.drawable.bed_icon,
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

                val iconRes = amenityIcons[amenity.lowercase()] ?: R.drawable.ic_empty
                iconView.setImageResource(iconRes)

                amenitiesContainer.addView(itemView)
            }

            val landlord = prop.landlordInfo

            if(landlord != null){
                binding.landlordTxt.text = landlord.firstName + " " + landlord.surname
            }
        }
        return property?.propertyId ?: "No Id"
    }

    @SuppressLint("SetTextI18n")
    private fun bindDataPassedFromWishlistFragment() : String {
        val property = intent.getSerializableExtra("property-wishList") as? FavouriteListingsResponse

        property?.let { prop ->
            binding.titleText.text = prop.listingDetail?.title
            binding.locationText.text = prop.listingDetail?.address
            binding.priceText.text = "R${prop.listingDetail?.price}"
            binding.propertyDescription.text = prop.listingDetail?.description

            val images = prop.listingDetail?.images ?: emptyList()

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

            val isFavourite = prop.listingDetail?.isFavourite ?: false
            updateFavouriteIcon(isFavourite)

            val amenityIcons = mapOf(
                "tv" to R.drawable.tv_icon,
                "wi-fi" to R.drawable.wifi_icon,
                "bed" to R.drawable.bed_icon,
            )

            val amenities = prop.listingDetail?.amenities ?: emptyList()
            val amenitiesContainer = binding.amenitiesContainer
            amenitiesContainer.removeAllViews()

            val inflater = LayoutInflater.from(this)

            for (amenity in amenities) {
                val itemView = inflater.inflate(R.layout.amenity_item, amenitiesContainer, false)

                val iconView = itemView.findViewById<ImageView>(R.id.amenityIcon)
                val textView = itemView.findViewById<TextView>(R.id.amenityText)

                textView.text = amenity

                val iconRes = amenityIcons[amenity.lowercase()] ?: R.drawable.ic_empty
                iconView.setImageResource(iconRes)

                amenitiesContainer.addView(itemView)
            }

            val landlord = prop.listingDetail?.landlordInfo

            if(landlord != null){
                binding.landlordTxt.text = landlord.firstName + " " + landlord.surname
            }
        }
        return property?.listingDetail?.listingID ?: "No Id"
    }

    private fun updateFavouriteIcon(isFavourited: Boolean) {
       if (isFavourited) {
           binding.favouriteBtn.setImageResource(R.drawable.favourite_icon_filled)
           binding.favouriteBtn.setColorFilter(ContextCompat.getColor(this, R.color.red))
       } else {
           binding.favouriteBtn.setImageResource(R.drawable.favourite_icon)
           binding.favouriteBtn.setColorFilter(ContextCompat.getColor(this, R.color.grey))
       }
    }
}