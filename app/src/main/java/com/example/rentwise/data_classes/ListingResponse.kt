package com.example.rentwise.data_classes

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LandLordDetails(
    @SerializedName("landlord")
    val landlordId: String?,
    val firstName: String?,
    val surname: String?,
    val phone: String?,
    val email: String?,
    val pfpImage: String?
) : Serializable

data class ListingResponse(
    @SerializedName("_id")
    val propertyId: String?,
    val title: String?,
    val address: String?,
    val description: String?,
    val imagesURL: List<String>?,
    val amenities: List<String>?,
    val price: Float?,
    @SerializedName("isFavourited")
    val isFavourite: Boolean?,
    val landlordInfo: LandLordDetails?,
    val averageRating: Float?,
    val reviewCount: Int?
) : Serializable
