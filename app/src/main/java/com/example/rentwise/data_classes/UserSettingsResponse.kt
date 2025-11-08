package com.example.rentwise.data_classes

import com.google.gson.annotations.SerializedName

data class UserSettingsResponse(
    @SerializedName("_id")
    val id: String?,
    val username: String?,
    val firstName: String?,
    val surname: String?,
    val email: String?,
    val phone: String?,
    val DoB: String?,
    val preferredLanguage: String?,
    val notifications: Boolean?,
    val offlineSync: Boolean?,
    val pfpImage: String?
)