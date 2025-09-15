package com.example.rentwise.api_interface

import com.example.rentwise.data_classes.BookingResponse
import com.example.rentwise.data_classes.BookingStatusResponse
import com.example.rentwise.data_classes.FavouriteListingPostResponse
import com.example.rentwise.data_classes.FavouriteListingsResponse
import com.example.rentwise.data_classes.LoginRequest
import com.example.rentwise.data_classes.LoginResponse
import com.example.rentwise.data_classes.ListingResponse
import com.example.rentwise.data_classes.NotificationResponse
import com.example.rentwise.data_classes.RegisterRequest
import com.example.rentwise.data_classes.RegisterResponse
import com.example.rentwise.data_classes.UnfavouriteListingResponse
import com.example.rentwise.data_classes.UserSettingsResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface RentWiseApi {
    @POST("/api/users/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("/api/users/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @GET("/api/listings")
    fun getListings(): Call<List<ListingResponse>>

    @GET("/api/listings/{id}")
    fun getListingById(@Path("id") listingId: String): Call<ListingResponse>

    @GET("/api/{userID}/favourites")
    fun getFavouriteListings(@Path("userID") userId: String): Call<MutableList<FavouriteListingsResponse>>

    @GET("/api/favourite/{listingID}")
    fun getFavouriteByListingId(@Path("listingID") listingId: String): Call<FavouriteListingsResponse>

    @GET("/api/users/{id}")
    fun getUserById(@Path("id") userId: String): Call<UserSettingsResponse>

    @POST("/api/{userID}/{listingID}/favourite")
    fun favouriteListing(@Path("userID") userId: String, @Path("listingID") listingId: String): Call<FavouriteListingPostResponse>

    @GET("/api/notifications")
    fun getNotifications() : Call<List<NotificationResponse>>

    @DELETE("/api/{userID}/{listingID}/unfavourite")
    fun deleteFavouriteListing(@Path("userID") userId: String, @Path("listingID") listingId: String): Call<UnfavouriteListingResponse>

    @Multipart
    @POST("/api/bookings/{userID}/{listingID}/create")
    fun createBooking(
        @Path("userID") userId: String,
        @Path("listingID") listingId: String,
        @Part("checkInDate") checkInDate: RequestBody,
        @Part("checkOutDate") checkOutDate: RequestBody,
        @Part("numberOfGuests") numberOfGuests: RequestBody,
        @Part supportDocuments: List<MultipartBody.Part>,
        @Part("totalPrice") totalPrice: RequestBody
    ): Call<BookingResponse>

    @GET("/api/bookings/{id}")
    fun getBookingById(@Path("id") userId: String): Call<BookingStatusResponse>
}