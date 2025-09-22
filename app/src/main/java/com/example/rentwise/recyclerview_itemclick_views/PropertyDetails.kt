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
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PropertyDetails : AppCompatActivity() {
    private lateinit var binding: ActivityPropertyDetailsBinding
    private var isFavourite = false
    private lateinit var tokenManger: TokenManger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPropertyDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManger = TokenManger(applicationContext)

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
            if(isFavourite){
                deleteFavouriteItemFromDbApiCall(listingId)
            } else{
                favouriteListing(listingId)
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
        binding.submitReviewButton.setOnClickListener {
            val rating = binding.ratingBar.rating.toInt()
            val comment = binding.reviewMessage.text.toString()
            submitReview(listingId, rating, comment)
        }
    }

    private fun compareWhichDataToBind() : String {
        val property = intent.getSerializableExtra("property") as? ListingResponse

        val listingId: String = if(property != null){ // Data passed from Home Fragment
            loadPropertyFromHomeFragment()
        } else{
            loadPropertyFromWishlistFragment() // Data passed from WishList Fragment
        }
        return listingId
    }

    @SuppressLint("SetTextI18n")
    private fun loadPropertyFromHomeFragment() : String {
        val property = intent.getSerializableExtra("property") as? ListingResponse
        getListingsAndBind(property?.propertyId ?: "")

        return property?.propertyId ?: "No Id"
    }

    @SuppressLint("SetTextI18n")
    private fun loadPropertyFromWishlistFragment() : String {
        val property = intent.getSerializableExtra("property-wishList") as? FavouriteListingsResponse
        getFavouriteListingsAndBind(property?.listingDetail?.listingID ?: "")

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
                    // Log error message
                    hideFavouriteLoading()
                    CustomToast.show(this@PropertyDetails, "${t.message}", CustomToast.Companion.ToastType.ERROR)
                    Log.e("Error", t.message.toString())
                }
            })
        }
    }

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
                        //Load first image from the images array and hide the rest if not available
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
                        }

                        binding.locationText.text = listing.address
                        binding.titleText.text = listing.title
                        binding.propertyDescription.text = listing.description
                        binding.priceText.text = "R${listing.price}"

                        isFavourite = listing.isFavourite ?: false
                        updateFavouriteIcon(isFavourite)

                        val amenityIcons = mapOf(
                            "tv" to R.drawable.tv_icon,
                            "wi-fi" to R.drawable.wifi_icon,
                            "bed" to R.drawable.bed_icon
                        )
                        val amenities = listing.amenities ?: emptyList()
                        val amenitiesContainer = binding.amenitiesContainer
                        amenitiesContainer.removeAllViews()

                        val inflater = LayoutInflater.from(this@PropertyDetails)
                        for (amenity in amenities) {
                            val itemView = inflater.inflate(R.layout.amenity_item, amenitiesContainer, false)
                            val iconView = itemView.findViewById<ImageView>(R.id.amenityIcon)
                            val textView = itemView.findViewById<TextView>(R.id.amenityText)

                            textView.text = amenity

                            val iconRes = amenityIcons[amenity.lowercase()] ?: R.drawable.ic_empty
                            iconView.setImageResource(iconRes)
                            amenitiesContainer.addView(itemView)
                        }

                        val landlordInfo = listing.landlordInfo
                        if (landlordInfo != null) {
                            binding.landlordTxt.text =
                                "${landlordInfo.firstName} ${landlordInfo.surname}"
                        }
                    }
                    CustomToast.show(this@PropertyDetails, "Property Loaded", CustomToast.Companion.ToastType.SUCCESS)
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
                    // Log out if unauthorized
                    if (response.code() == 401) {
                        tokenManger.clearToken()
                        tokenManger.clearUser()

                        val intent = Intent(this@PropertyDetails, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //Clear Activity trace
                        startActivity(intent)
                    }
                    CustomToast.show(this@PropertyDetails, errorMessage, CustomToast.Companion.ToastType.ERROR)
                }
            }

            override fun onFailure(call: Call<ListingResponse>, t: Throwable) {
                // Log error message
                hideLoading()
                CustomToast.show(this@PropertyDetails, "${t.message}", CustomToast.Companion.ToastType.ERROR)
                Log.e("Error", t.message.toString())
            }
        })
    }
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
                        }
                        binding.locationText.text = property.listingDetail?.address ?: ""
                        binding.titleText.text = property.listingDetail?.title ?: ""
                        binding.priceText.text = "R${property.listingDetail?.price}"

                        isFavourite = property.listingDetail?.isFavourite ?: false
                        updateFavouriteIcon(isFavourite)

                        val amenityIcons = mapOf(
                            "tv" to R.drawable.tv_icon,
                            "wi-fi" to R.drawable.wifi_icon,
                            "bed" to R.drawable.bed_icon
                        )
                        val amenities = property.listingDetail?.amenities ?: emptyList()
                        val amenitiesContainer = binding.amenitiesContainer
                        amenitiesContainer.removeAllViews()

                        val inflater = LayoutInflater.from(this@PropertyDetails)
                        for (amenity in amenities) {
                            val itemView = inflater.inflate(R.layout.amenity_item, amenitiesContainer, false)
                            val iconView = itemView.findViewById<ImageView>(R.id.amenityIcon)
                            val textView = itemView.findViewById<TextView>(R.id.amenityText)

                            textView.text = amenity

                            val iconRes = amenityIcons[amenity.lowercase()] ?: R.drawable.ic_empty
                            iconView.setImageResource(iconRes)
                            amenitiesContainer.addView(itemView)
                        }

                        val landlordInfo = property.listingDetail?.landlordInfo
                        if (landlordInfo != null) {
                            binding.landlordTxt.text =
                                "${landlordInfo.firstName} ${landlordInfo.surname}"
                        }
                    }
                    CustomToast.show(this@PropertyDetails, "Property Loaded", CustomToast.Companion.ToastType.SUCCESS)
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
                    // Log out if unauthorized
                    if (response.code() == 401) {
                        tokenManger.clearToken()
                        tokenManger.clearUser()

                        val intent = Intent(this@PropertyDetails, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //Clear Activity trace
                        startActivity(intent)
                    }
                    CustomToast.show(this@PropertyDetails, errorMessage, CustomToast.Companion.ToastType.ERROR)
                }
            }

            override fun onFailure(
                call: Call<FavouriteListingsResponse>,
                t: Throwable
            ) {
                // Log error message
                hideLoading()
                CustomToast.show(this@PropertyDetails, "${t.message}", CustomToast.Companion.ToastType.ERROR)
                Log.e("Error", t.message.toString())
            }
        })
    }

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
                            // Successfully unfavourited
                            isFavourite = !isFavourite
                            responseBody.message?.let {
                                CustomToast.show(this@PropertyDetails, it, CustomToast.Companion.ToastType.SUCCESS)
                            }
                            updateFavouriteIcon(isFavourite)
                        }
                    }
                    else{
                        // Handle error response
                        hideFavouriteLoading()
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = if (errorBody != null) {
                            try {
                                val json = JSONObject(errorBody)
                                when { // Check for both "message" and "error" keys
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
                        }

                    }
                    else{
                        // Handle error response
                        hideFavouriteLoading()
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = if (errorBody != null) {
                            try {
                                val json = JSONObject(errorBody)
                                when { // Check for both "message" and "error" keys
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

                override fun onFailure(
                    call: Call<ReviewResponse?>,
                    t: Throwable
                ) {
                    hideFavouriteLoading()
                    CustomToast.show(this@PropertyDetails, "Error: ${t.message.toString()}", CustomToast.Companion.ToastType.ERROR)
                    Log.e("Error", t.message.toString())
                }

            }
            )
        }

    }


    private fun showLoading() {
        binding.loadingOverlay.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingOverlay.visibility = View.GONE
    }

    private fun showFavouriteLoading() {
        binding.favouriteOverlay.visibility = View.VISIBLE
    }

    private fun hideFavouriteLoading() {
        binding.favouriteOverlay.visibility = View.GONE
    }
}