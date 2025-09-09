package com.example.rentwise.api_interface

import com.example.rentwise.data_classes.LoginRequest
import com.example.rentwise.data_classes.LoginResponse
import com.example.rentwise.data_classes.ListingResponse
import com.example.rentwise.data_classes.RegisterRequest
import com.example.rentwise.data_classes.RegisterResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface RentWiseApi {
    @POST("/api/users/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("/api/users/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @GET("/api/listings")
    fun getListings(): Call<List<ListingResponse>>
}