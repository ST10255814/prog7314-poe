package com.example.rentwise.data_classes

data class RegisterResponse(
    val email: String?,
    val message: String?,
    val firstName: String?,   // [FIX] use firstName (camel N)
    val surname: String?
)
