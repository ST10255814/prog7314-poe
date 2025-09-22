package com.example.rentwise.data_classes

import com.google.gson.annotations.SerializedName

data class UserSettingsResponse(
    @SerializedName("_id")
    val id: String?,
    val userId: String?,
    val profile: Profile?
)

data class Profile(
    val username: String?,
    val firstName: String?,
    val surname: String?,
    val email: String?,
    val phone: String?,
    val DoB: String?,
    val preferredLanguage: String?,
    val pfpImage: String?
)