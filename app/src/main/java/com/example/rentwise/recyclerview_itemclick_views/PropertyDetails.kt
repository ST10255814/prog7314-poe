package com.example.rentwise.recyclerview_itemclick_views

import com.example.rentwise.retrofit_instance.RetrofitInstance
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.example.rentwise.auth.LoginActivity
import com.example.rentwise.booking.Booking
import com.example.rentwise.custom_toast.CustomToast
import com.example.rentwise.data_classes.CreateReviewRequest
import com.example.rentwise.data_classes.FavouriteListingPostResponse
import com.example.rentwise.data_classes.FavouriteListingsResponse
import com.example.rentwise.data_classes.ListingResponse
import com.example.rentwise.data_classes.ReviewResponse
import com.example.rentwise.data_classes.UnfavouriteListingResponse
import com.example.rentwise.databinding.ActivityPropertyDetailsBinding
import com.example.rentwise.home.HomeScreen
import com.example.rentwise.shared_pref_config.TokenManger
import com.example.rentwise.utils.LocaleHelper
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Activity for displaying detailed property information, handling user interactions such as booking, favouriting, and reviewing.
class PropertyDetails : AppCompatActivity() {
    // Binds the layout for property details, providing access to all UI elements.
    private lateinit var binding: ActivityPropertyDetailsBinding
    // Tracks whether the property is currently favourited by the user.
    private var isFavourite = false
    // Manages user authentication tokens and session data.
    private lateinit var tokenManger: TokenManger

    // Maps amenity names to their corresponding icon resources for dynamic UI rendering.
    private lateinit var amenityLabelByKey: Map<String, String>

    private val amenityIconByKey: Map<String, Int> = mapOf(
        "swimming_pool" to R.drawable.ic_amenity_pool,
        "gym" to R.drawable.ic_amenity_gym,
        "parking" to R.drawable.ic_amenity_parking,
        "security_24_7" to R.drawable.ic_amenity_security,
        "air_conditioning" to R.drawable.ic_amenity_ac,
        "balcony" to R.drawable.ic_amenity_balcony,
        "garden" to R.drawable.garden_icon,
        "laundry_room" to R.drawable.ic_amenity_laundry,
        "pet_friendly" to R.drawable.ic_amenity_pet,
        "wi-fi" to R.drawable.wifi_icon,
        "wifi" to R.drawable.wifi_icon,
        "furnished" to R.drawable.ic_amenity_furnished,
        "kitchen_appliances" to R.drawable.ic_amenity_kitchen,
        "elevator" to R.drawable.ic_amenity_elevator,
        "Playground" to R.drawable.ic_amenity_playground,
        "bbq_area" to R.drawable.ic_amenity_bbq,
        "storage_room" to R.drawable.ic_amenity_storage,
        "garage" to R.drawable.garage_icon,
        "dishwasher" to R.drawable.ic_amenity_dishwasher,
        "washing_machine" to R.drawable.ic_amenity_washing_machine,
        "microwave" to R.drawable.ic_amenity_microwave,
        "refrigerator" to R.drawable.ic_amenity_fridge,
        "tv" to R.drawable.tv_icon,
        "public_transport" to R.drawable.ic_amenity_transport,
        "shopping_mall" to R.drawable.ic_amenity_mall,
        "school" to R.drawable.ic_amenity_school,
        "hospital" to R.drawable.ic_amenity_hospital,
        "fireplace" to R.drawable.ic_amenity_fireplace,
        "study_room" to R.drawable.ic_amenity_study,
        "backup_generator" to R.drawable.ic_amenity_generator,
        "water_tank" to R.drawable.ic_amenity_water_tank,
        "cctv" to R.drawable.ic_amenity_cctv,
        "intercom" to R.drawable.ic_amenity_intercom,
        "cleaning_service" to R.drawable.ic_amenity_cleaning,
        "maintenance_service" to R.drawable.ic_amenity_maintenance,
        "bed" to R.drawable.bed_icon,
    )

    // Normalizes amenity keys to handle variations in naming
    private fun canonicalAmenityKey(rawAmenity: String): String {
        return rawAmenity.lowercase()
            .replace(" ", "_")
            .replace("-", "_")
            .replace("wi_fi", "wifi")
            .replace("24/7_security", "security_24_7")
            .replace("air_con", "air_conditioning")
            .replace("ac", "air_conditioning")
    }


    // This ensures the saved language is applied when the activity is created
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPropertyDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManger = TokenManger(applicationContext)

        // Initialize amenity labels map
        amenityLabelByKey = mapOf(
            "swimming_pool" to "Swimming Pool",
            "gym" to "Gym",
            "parking" to "Parking",
            "security_24_7" to "24/7 Security",
            "air_conditioning" to "Air Conditioning",
            "balcony" to "Balcony",
            "garden" to "Garden",
            "laundry_room" to "Laundry Room",
            "pet_friendly" to "Pet Friendly",
            "wi-fi" to "Wi-Fi",
            "wifi" to "Wi-Fi",
            "furnished" to "Furnished",
            "kitchen_appliances" to "Kitchen Appliances",
            "elevator" to "Elevator",
            "playground" to "Playground",
            "bbq_area" to "BBQ Area",
            "storage_room" to "Storage Room",
            "garage" to "Garage",
            "dishwasher" to "Dishwasher",
            "washing_machine" to "Washing Machine",
            "microwave" to "Microwave",
            "refrigerator" to "Refrigerator",
            "tv" to "TV",
            "public_transport" to "Public Transport",
            "shopping_mall" to "Shopping Mall",
            "school" to "School",
            "hospital" to "Hospital",
            "fireplace" to "Fireplace",
            "study_room" to "Study Room",
            "backup_generator" to "Backup Generator",
            "water_tank" to "Water Tank",
            "cctv" to "CCTV",
            "intercom" to "Intercom",
            "cleaning_service" to "Cleaning Service",
            "maintenance_service" to "Maintenance Service",
            "bed" to "Bed"
        )

        val listingId = compareWhichDataToBind() // Determines which property data to display.
        setButtonListeners(listingId) // Sets up all button and touch listeners for user actions.
    }

    @SuppressLint("ClickableViewAccessibility")
    // Attaches click and touch listeners for all interactive UI elements, including booking, favouriting, and reviewing.
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

        // Call landlord (opens dialer)
        binding.callButton.setOnClickListener {
            val phone = binding.landlordTxt.tag as? String           // phone is stashed later when binding landlord
            if (!phone.isNullOrBlank()) {
                val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                startActivity(dial)
            } else {
                CustomToast.show(this, getString(R.string.no_phone_available), CustomToast.Companion.ToastType.ERROR)
            }
        }

        // Chat landlord (prefer SMS; else email)
        binding.chatButton.setOnClickListener {
            val phone = binding.landlordTxt.tag as? String
            val email = binding.landlordTxt.getTag(R.id.tag_email) as? String
            val displayName = binding.landlordTxt.text?.toString()?.trim()?.ifBlank { getString(R.string.landlord) }

            when {
                !phone.isNullOrBlank() -> {
                    val smsUri = Uri.parse("smsto:$phone")
                    val sms = Intent(Intent.ACTION_SENDTO, smsUri)
                    sms.putExtra("sms_body", getString(R.string.sms_template, displayName))
                    startActivity(sms)
                }
                !email.isNullOrBlank() -> {
                    val mail = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                    mail.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_subject))
                    mail.putExtra(Intent.EXTRA_TEXT, getString(R.string.mail_body, displayName))
                    startActivity(mail)
                }
                else -> {
                    CustomToast.show(this, getString(R.string.no_contact_method), CustomToast.Companion.ToastType.ERROR)
                }
            }
        }

        binding.favouriteBtn.setOnClickListener {
            if(isFavourite){
                deleteFavouriteItemFromDbApiCall(listingId) // Unfavourite property.
            } else{
                favouriteListing(listingId) // Favourite property.
            }
        }
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
            finish()
        }
        binding.buttonBookNow.setOnClickListener {
            val intent = Intent(this, Booking::class.java)
            intent.putExtra("propertyId", listingId)
            startActivity(intent)
            finish()
        }
        binding.ratingBar.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).start()
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
        binding.submitReviewButton.setOnClickListener {
            val rating = binding.ratingBar.rating.toInt()
            val comment = binding.reviewMessage.text.toString()
            submitReview(listingId, rating, comment) // Submits a review for the property.
        }
    }

    // Determines which intent extra was received and loads the appropriate property data.
    private fun compareWhichDataToBind() : String {
        val property = intent.getSerializableExtra("property") as? ListingResponse

        val listingId: String = if(property != null){
            loadPropertyFromHomeFragment() // Data from Home Fragment.
        } else{
            loadPropertyFromWishlistFragment() // Data from Wishlist Fragment.
        }
        return listingId
    }

    @SuppressLint("SetTextI18n")
    // Loads and binds property data for unfavourited properties (from Home Fragment).
    private fun loadPropertyFromHomeFragment() : String {
        val property = intent.getSerializableExtra("property") as? ListingResponse
        getListingsAndBind(property?.propertyId ?: "")
        return property?.propertyId ?: "No Id"
    }

    @SuppressLint("SetTextI18n")
    // Loads and binds property data for favourited properties (from Wishlist Fragment).
    private fun loadPropertyFromWishlistFragment() : String {
        val property = intent.getSerializableExtra("property-wishList") as? FavouriteListingsResponse
        getFavouriteListingsAndBind(property?.listingDetail?.listingID ?: "")
        return property?.listingDetail?.listingID ?: "No Id"
    }

    // Updates the favourite button UI based on the current favourite state.
    private fun updateFavouriteIcon(isFavourited: Boolean) {
        if (isFavourited) {
            binding.favouriteBtn.setImageResource(R.drawable.favourite_icon_filled)
            binding.favouriteBtn.setColorFilter(ContextCompat.getColor(this, R.color.red))
        } else {
            binding.favouriteBtn.setImageResource(R.drawable.favourite_icon)
            binding.favouriteBtn.setColorFilter(ContextCompat.getColor(this, R.color.grey))
        }
    }

    // Sends a request to favourite the property and updates UI/animations on success.
    private fun favouriteListing(listingId: String){
        showFavouriteLoading()
        val userId = tokenManger.getUser()
        if(userId != null){
            val api = RetrofitInstance.createAPIInstance(applicationContext)
            api.favouriteListing(userId, listingId).enqueue( object: Callback<FavouriteListingPostResponse>{
                override fun onResponse(
                    call: Call<FavouriteListingPostResponse>,
                    response: Response<FavouriteListingPostResponse>
                ) {
                    if(response.isSuccessful){
                        hideFavouriteLoading()
                        val apiResponse = response.body()
                        if (apiResponse != null){
                            apiResponse.message?.let {
                                CustomToast.show(this@PropertyDetails, it, CustomToast.Companion.ToastType.SUCCESS)
                            }
                            isFavourite = !isFavourite
                            updateFavouriteIcon(isFavourite)
                            // Animates the favourite button for visual feedback.
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
                        hideFavouriteLoading()
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
                        CustomToast.show(this@PropertyDetails, errorMessage, CustomToast.Companion.ToastType.ERROR)
                        Log.e("Error", errorMessage)
                        // Handles authentication errors by clearing session and redirecting to login.
                        if (response.code() == 401) {
                            tokenManger.clearToken()
                            tokenManger.clearUser()
                            tokenManger.clearPfp()
                            val intent = Intent(this@PropertyDetails, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                }

                override fun onFailure(call: Call<FavouriteListingPostResponse>, t: Throwable) {
                    hideFavouriteLoading()
                    CustomToast.show(this@PropertyDetails, "${t.message}", CustomToast.Companion.ToastType.ERROR)
                    Log.e("Error", t.message.toString())
                }
            })
        }
    }

    // Fetches property details by ID and binds all relevant data to the UI.
    private fun getListingsAndBind(listingId : String){
        showLoading()
        val api = RetrofitInstance.createAPIInstance(applicationContext)
        api.getListingById(listingId).enqueue( object : Callback<ListingResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<ListingResponse>,
                response: Response<ListingResponse>
            ) {
                hideLoading()
                if(response.isSuccessful) {
                    response.body()?.let { listing ->
                        // Loads main and extra property images, hiding unused image views.
                        val images = listing.imagesURL ?: emptyList()
                        val extraPhotos = listOf(binding.image2, binding.image3, binding.image4)
                        if (images.isNotEmpty()) {
                            Glide.with(this@PropertyDetails)
                                .load(images[0])
                                .placeholder(R.drawable.ic_empty)
                                .error(R.drawable.ic_empty)
                                .into(binding.imageMain)
                            images.drop(1).take(3).forEachIndexed { index, imageUrl ->
                                Glide.with(this@PropertyDetails)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_empty)
                                    .error(R.drawable.ic_empty)
                                    .into(extraPhotos[index])
                            }
                            for (i in images.drop(1).size until 3) {
                                extraPhotos[i].visibility = View.GONE
                            }
                            // location line now prefers "area · address"
                            val area = listing.area?.takeIf { it.isNotBlank() }
                            val address = listing.address?.takeIf { it.isNotBlank() }
                            binding.locationText.text = when {
                                area != null && address != null -> getString(R.string.address_area_sep, area, address)
                                area != null -> area
                                else -> address
                            }
                        }
                        binding.locationText.text = listing.address
                        binding.titleText.text = listing.title
                        binding.propertyDescription.text = listing.description
                        binding.priceText.text = getString(R.string.currency_rand_amount_decimal, listing.price ?: 0f)

                        isFavourite = listing.isFavourite ?: false
                        updateFavouriteIcon(isFavourite)

                        // Shoes RATING on DETAILS (avg + count) if available.
                        val avg = listing.averageRating
                        val count = listing.reviewCount
                        if (avg != null && avg > 0f) {
                            binding.ratingRow.visibility = View.VISIBLE
                            binding.tvAvgRating.text = String.format("%.1f", avg)
                            binding.tvReviewCount.text = if (count != null && count > 0)
                                getString(R.string.rating_count_fmt, count) else ""
                            binding.ratingRow.visibility = View.GONE
                        }

                        // Dynamically adds amenity icons and labels to the UI.
                        // Use the LISTING response for amenities (not 'property'), and normalize
                        val amenities: List<String> = listing.amenities ?: emptyList()
                        val container = binding.amenitiesContainer
                        container.removeAllViews()
                        val inflater = LayoutInflater.from(this@PropertyDetails)

                        for (rawAmenity in amenities) {
                            val key = canonicalAmenityKey(rawAmenity)
                            val label = amenityLabelByKey[key] ?: rawAmenity
                            val iconRes = amenityIconByKey[key] ?: R.drawable.ic_empty

                            val itemView = inflater.inflate(R.layout.amenity_item, container, false)
                            val iconView = itemView.findViewById<ImageView>(R.id.amenityIcon)
                            val textView = itemView.findViewById<TextView>(R.id.amenityText)

                            iconView.setImageResource(iconRes)
                            textView.text = label

                            container.addView(itemView)
                        }

                        // Loads landlord information and profile image if available.
                        val landlordInfo = listing.landlordInfo
                        if (landlordInfo != null) {
                            binding.landlordTxt.text =
                                "${landlordInfo.firstName} ${landlordInfo.surname}"
                            binding.landlordTxt.tag = landlordInfo.phone                 // store phone for Call/SMS
                            binding.landlordTxt.setTag(R.id.tag_email, landlordInfo.email) // store email for fallback chat
                            Glide.with(this@PropertyDetails)
                                .load(landlordInfo.pfpImage)
                                .placeholder(R.drawable.profile_icon)
                                .error(R.drawable.profile_icon)
                                .circleCrop()
                                .into(binding.estateAgent)
                        }
                    }
                    CustomToast.show(this@PropertyDetails, getString(R.string.property_loaded), CustomToast.Companion.ToastType.SUCCESS)
                }
                else {
                    hideLoading()
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (errorBody != null) {
                        try {
                            val json = JSONObject(errorBody)
                            json.getString("error")
                        } catch (e: Exception) {
                            "Unknown error"
                        }
                    } else {
                        "Unknown error"
                    }
                    // Handles authentication errors by clearing session and redirecting to login.
                    if (response.code() == 401) {
                        tokenManger.clearToken()
                        tokenManger.clearUser()
                        tokenManger.clearPfp()
                        val intent = Intent(this@PropertyDetails, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    CustomToast.show(this@PropertyDetails, errorMessage, CustomToast.Companion.ToastType.ERROR)
                }
            }

            override fun onFailure(call: Call<ListingResponse>, t: Throwable) {
                hideLoading()
                CustomToast.show(this@PropertyDetails, "${t.message}", CustomToast.Companion.ToastType.ERROR)
                Log.e("Error", t.message.toString())
            }
        })
    }

    // Fetches and binds details for a favourited property by its listing ID.
    private fun getFavouriteListingsAndBind(listingId : String){
        showLoading()
        val api = RetrofitInstance.createAPIInstance(applicationContext)
        api.getFavouriteByListingId(listingId).enqueue( object : Callback<FavouriteListingsResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(
                call: Call<FavouriteListingsResponse>,
                response: Response<FavouriteListingsResponse>
            ) {
                if(response.isSuccessful) {
                    hideLoading()
                    response.body()?.let { property ->
                        val images = property.listingDetail?.images ?: emptyList()
                        val extraPhotos = listOf(binding.image2, binding.image3, binding.image4)
                        if (images.isNotEmpty()) {
                            Glide.with(this@PropertyDetails)
                                .load(images[0])
                                .placeholder(R.drawable.ic_empty)
                                .error(R.drawable.ic_empty)
                                .into(binding.imageMain)
                            images.drop(1).take(3).forEachIndexed { index, imageUrl ->
                                Glide.with(this@PropertyDetails)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_empty)
                                    .error(R.drawable.ic_empty)
                                    .into(extraPhotos[index])
                            }
                            for (i in images.drop(1).size until 3) {
                                extraPhotos[i].visibility = View.GONE
                            }
                            // location line now prefers "area · address"
                            val area = property.listingDetail?.area?.takeIf { it.isNotBlank() }
                            val address = property.listingDetail?.address?.takeIf { it.isNotBlank() }
                            binding.locationText.text = when {
                                area != null && address != null -> getString(R.string.address_area_sep, area, address)
                                area != null -> area
                                else -> address ?: ""
                            }
                        }
                        binding.locationText.text = property.listingDetail?.address ?: ""
                        binding.titleText.text = property.listingDetail?.title ?: ""
                        binding.propertyDescription.text = property.listingDetail?.description ?: ""
                        binding.priceText.text = getString(
                            R.string.currency_rand_amount_decimal,
                            property.listingDetail?.price ?: 0f
                        )

                        // Show rating if available from wishlist payload
                        isFavourite = property.listingDetail?.isFavourite ?: false
                        updateFavouriteIcon(isFavourite)

                        // Shows RATING on DETAILS if available from wishlist payload.
                        val avg = property.listingDetail?.averageRating
                        val count = property.listingDetail?.reviewCount
                        if (avg != null && avg > 0f) {
                            binding.ratingRow.visibility = View.VISIBLE
                            binding.tvAvgRating.text = String.format("%.1f", avg)
                            binding.tvReviewCount.text = if (count != null && count > 0)
                                getString(R.string.rating_count_fmt, count) else ""
                        } else {
                            binding.ratingRow.visibility = View.GONE
                        }

                        // Normalize + map icons/labels (replace old 'amenityIcons' usage)
                        val amenities: List<String> = property.listingDetail?.amenities ?: emptyList()
                        val container = binding.amenitiesContainer
                        container.removeAllViews()
                        val inflater = LayoutInflater.from(this@PropertyDetails)

                        for (rawAmenity in amenities) {
                            val key = canonicalAmenityKey(rawAmenity)
                            val label = amenityLabelByKey[key] ?: rawAmenity
                            val iconRes = amenityIconByKey[key] ?: R.drawable.ic_empty

                            val itemView = inflater.inflate(R.layout.amenity_item, container, false)
                            val iconView = itemView.findViewById<ImageView>(R.id.amenityIcon)
                            val textView = itemView.findViewById<TextView>(R.id.amenityText)

                            iconView.setImageResource(iconRes)
                            textView.text = label

                            container.addView(itemView)
                        }

                        val landlordInfo = property.listingDetail?.landlordInfo
                        if (landlordInfo != null) {
                            binding.landlordTxt.text =
                                "${landlordInfo.firstName} ${landlordInfo.surname}"
                            binding.landlordTxt.tag = landlordInfo.phone
                            binding.landlordTxt.setTag(R.id.tag_email, landlordInfo.email)
                            Glide.with(this@PropertyDetails)
                                .load(landlordInfo.pfpImage)
                                .placeholder(R.drawable.profile_icon)
                                .error(R.drawable.profile_icon)
                                .circleCrop()
                                .into(binding.estateAgent)
                        }
                    }
                    CustomToast.show(this@PropertyDetails, getString(R.string.property_loaded), CustomToast.Companion.ToastType.SUCCESS)
                }
                else {
                    hideLoading()
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (errorBody != null) {
                        try {
                            val json = JSONObject(errorBody)
                            json.getString("error")
                        } catch (e: Exception) {
                            "Unknown error"
                        }
                    } else {
                        "Unknown error"
                    }
                    if (response.code() == 401) {
                        tokenManger.clearToken()
                        tokenManger.clearUser()
                        tokenManger.clearPfp()
                        val intent = Intent(this@PropertyDetails, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    CustomToast.show(this@PropertyDetails, errorMessage, CustomToast.Companion.ToastType.ERROR)
                }
            }

            override fun onFailure(
                call: Call<FavouriteListingsResponse>,
                t: Throwable
            ) {
                hideLoading()
                CustomToast.show(this@PropertyDetails, "${t.message}", CustomToast.Companion.ToastType.ERROR)
                Log.e("Error", t.message.toString())
            }
        })
    }

    // Sends a request to unfavourite the property and updates UI on success.
    private fun deleteFavouriteItemFromDbApiCall(listingId: String?){
        showFavouriteLoading()
        val userId = tokenManger.getUser()
        val api = RetrofitInstance.createAPIInstance(applicationContext)
        if(userId != null && listingId != null){
            api.deleteFavouriteListing(userId, listingId).enqueue(object : Callback<UnfavouriteListingResponse> {
                override fun onResponse(
                    call: Call<UnfavouriteListingResponse>,
                    response: Response<UnfavouriteListingResponse>
                ) {
                    if (response.isSuccessful) {
                        hideFavouriteLoading()
                        val responseBody = response.body()
                        if (responseBody != null) {
                            isFavourite = !isFavourite
                            responseBody.message?.let {
                                CustomToast.show(this@PropertyDetails, it, CustomToast.Companion.ToastType.SUCCESS)
                            }
                            updateFavouriteIcon(isFavourite)
                        }
                    }
                    else{
                        hideFavouriteLoading()
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
                        CustomToast.show(this@PropertyDetails, errorMessage, CustomToast.Companion.ToastType.ERROR)
                        Log.e("Error", errorMessage)
                        if (response.code() == 401) {
                            tokenManger.clearToken()
                            tokenManger.clearUser()
                            tokenManger.clearPfp()
                            val intent = Intent(this@PropertyDetails, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                }
                override fun onFailure(
                    call: Call<UnfavouriteListingResponse>,
                    t: Throwable
                ) {
                    hideFavouriteLoading()
                    CustomToast.show(this@PropertyDetails, "Error: ${t.message.toString()}", CustomToast.Companion.ToastType.ERROR)
                    Log.e("Error", t.message.toString())
                }
            })
        }
    }

    // Submits a review for the property, handling both success and error responses.
    @SuppressLint("SuspiciousIndentation")
    private fun submitReview(listingId: String, rating: Int, comment: String) {
        val userId = tokenManger.getUser()
        if(userId != null){
            val api = RetrofitInstance.createAPIInstance(applicationContext)
            val request = CreateReviewRequest(
                rating = rating,
                comment = comment
            )
            api.createReview(userId, listingId, request).enqueue(object : Callback<ReviewResponse> {
                override fun onResponse(
                    call: Call<ReviewResponse?>,
                    response: Response<ReviewResponse?>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        responseBody?.message?.let {
                            CustomToast.show(this@PropertyDetails, it, CustomToast.Companion.ToastType.SUCCESS)
                            binding.ratingBar.rating = 0f
                            binding.reviewMessage.text.clear()
                        }
                    }
                    else{
                        hideFavouriteLoading()
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
                        CustomToast.show(this@PropertyDetails, errorMessage, CustomToast.Companion.ToastType.ERROR)
                        Log.e("Error", errorMessage)
                        if (response.code() == 401) {
                            tokenManger.clearToken()
                            tokenManger.clearUser()
                            tokenManger.clearPfp()
                            val intent = Intent(this@PropertyDetails, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }
                }

                override fun onFailure(
                    call: Call<ReviewResponse?>,
                    t: Throwable
                ) {
                    hideFavouriteLoading()
                    CustomToast.show(this@PropertyDetails, "Error: ${t.message.toString()}", CustomToast.Companion.ToastType.ERROR)
                    Log.e("Error", t.message.toString())
                }
            })
        }
    }

    // Shows a loading overlay while property details are being fetched.
    private fun showLoading() {
        binding.loadingOverlay.visibility = View.VISIBLE
    }

    // Hides the loading overlay after property details are loaded.
    private fun hideLoading() {
        binding.loadingOverlay.visibility = View.GONE
    }

    // Shows a loading overlay while favourite/unfavourite actions are being processed.
    private fun showFavouriteLoading() {
        binding.favouriteOverlay.visibility = View.VISIBLE
    }

    // Hides the favourite loading overlay after actions are complete.
    private fun hideFavouriteLoading() {
        binding.favouriteOverlay.visibility = View.GONE
    }
}
