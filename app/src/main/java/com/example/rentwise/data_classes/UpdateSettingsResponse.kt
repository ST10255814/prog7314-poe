package com.example.rentwise.data_classes

data class UpdateSettingsResponse(
    val message: String?,
    val profile: ProfileResponse?
)

data class ProfileResponse(
    val pfpImage: String?
)
