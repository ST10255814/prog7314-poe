package com.example.rentwise.data_classes

import com.google.gson.annotations.SerializedName
import okhttp3.Address
import java.io.Serializable

//declaring what we fetching from the API
data class FavouriteListingsResponse(
    @SerializedName("_id")
    val favouriteId: String?,
    val userId: String?,
    val listingDetail: ListingDetails?,
) : Serializable

data class ListingDetails(
    val listingID: String?,
    val title: String?,
    val address: String?,
    val description: String?,
    val images: List<String>?,
    val amenities: List<String>?,
    val price: Float?,
    @SerializedName("isFavourited")
    val isFavourite: Boolean?,
    val landlordInfo: LandLordDetails?,
    val averageRating: Float?,
    val reviewCount: Int?
) : Serializable

data class FavouriteListingPostResponse(
    val message: String?,
    val favouriteId: String?
)
