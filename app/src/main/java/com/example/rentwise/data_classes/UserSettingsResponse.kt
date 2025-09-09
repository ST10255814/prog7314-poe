package com.example.rentwise.data_classes

import com.google.gson.annotations.SerializedName

data class UserSettingsResponse(
    @SerializedName("_id")
    val id: String?,
    val userId: String?,
    val email: String?,
    @SerializedName("DoB")
    val dob: String?,
    val firstName: String?,
    val phone: String?,
    val surname: String?,
    val username: String?
)