package com.example.rentwise.data_classes

data class GoogleResponse(
    val success: Boolean?,
    val token: String?,
    val user: User?
)

data class User(
    val id: String?,
    val name: String?,
    val email: String?,
    val pfpImage: String?
)

data class GoogleRequest(
    val idToken: String?
)

