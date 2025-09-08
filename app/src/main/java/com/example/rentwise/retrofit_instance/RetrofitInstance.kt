package com.example.rentwise.retrofit_instance

import com.example.rentwise.api_interface_auth.UserAuth
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://rentwiseapi.onrender.com"

    val instance: UserAuth by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(UserAuth::class.java)
    }
}