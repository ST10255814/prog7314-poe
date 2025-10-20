package com.example.rentwise.data_classes

data class LoginResponse(
    val message: String?,
    val success: Boolean?,
    val data: LoginData?
)
data class LoginData(
    val token: String?,
    val userId: String?
)
