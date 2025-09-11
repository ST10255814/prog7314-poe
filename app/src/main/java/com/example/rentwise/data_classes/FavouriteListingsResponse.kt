package com.example.rentwise.data_classes

import com.google.gson.annotations.SerializedName
import java.io.Serializable

//declaring what we fetching from the API
data class FavouriteListingsResponse(
    @SerializedName("_id")
    val favouriteListingId: String?,
    val listingId: String?,
    val favouriteListing: ListingResponse?
) : Serializable

