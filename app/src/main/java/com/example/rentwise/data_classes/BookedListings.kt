package com.example.rentwise.data_classes

import com.google.gson.annotations.SerializedName

data class BookedListings(
    @SerializedName("_id")
    val listingId: String,
    val title: String,
)
