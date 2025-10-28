package com.example.rentwise.api_interface

import com.example.rentwise.data_classes.BookingResponse
import com.example.rentwise.data_classes.BookingStatusResponse
import com.example.rentwise.data_classes.CreateReviewRequest
import com.example.rentwise.data_classes.FavouriteListingPostResponse
import com.example.rentwise.data_classes.FavouriteListingsResponse
import com.example.rentwise.data_classes.GoogleRequest
import com.example.rentwise.data_classes.GoogleResponse
import com.example.rentwise.data_classes.LoginRequest
import com.example.rentwise.data_classes.LoginResponse
import com.example.rentwise.data_classes.ListingResponse
import com.example.rentwise.data_classes.MaintenanceRequestResponse
import com.example.rentwise.data_classes.MaintenanceResponse
import com.example.rentwise.data_classes.NotificationResponse
import com.example.rentwise.data_classes.RegisterRequest
import com.example.rentwise.data_classes.RegisterResponse
import com.example.rentwise.data_classes.ReviewResponse
import com.example.rentwise.data_classes.UnfavouriteListingResponse
import com.example.rentwise.data_classes.UpdateSettingsResponse
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

// Retrofit interface defining all API endpoints for the RentWise application.
// Each method corresponds to a specific backend route, handling user authentication, listings, bookings, maintenance, notifications, reviews, and user settings.
interface RentWiseApi {
    // Authenticates a user with email and password, returning a login response with user details and token.
    @POST("/api/users/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    // Registers a new user with provided registration details, returning a registration response.
    @POST("/api/users/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    // Retrieves a list of all property listings available in the system.
    @GET("/api/listings")
    fun getListings(): Call<List<ListingResponse>>

    // Fetches detailed information for a specific listing by its unique ID.
    @GET("/api/listings/{id}")
    fun getListingById(@Path("id") listingId: String): Call<ListingResponse>

    // Gets all favourite listings for a specific user, identified by user ID.
    @GET("/api/{userID}/favourites")
    fun getFavouriteListings(@Path("userID") userId: String): Call<MutableList<FavouriteListingsResponse>>

    // Retrieves a favourite listing by its associated listing ID.
    @GET("/api/favourite/{listingID}")
    fun getFavouriteByListingId(@Path("listingID") listingId: String): Call<FavouriteListingsResponse>

    // Fetches user profile and settings information by user ID.
    @GET("/api/users/{id}")
    fun getUserById(@Path("id") userId: String): Call<UserSettingsResponse>

    // Adds a listing to a user's favourites, using user and listing IDs, and returns the result.
    @POST("/api/{userID}/{listingID}/favourite")
    fun favouriteListing(@Path("userID") userId: String, @Path("listingID") listingId: String): Call<FavouriteListingPostResponse>

    // Retrieves all notifications for the current user session.
    @GET("/api/notifications")
    fun getNotifications() : Call<List<NotificationResponse>>

    // Removes a listing from a user's favourites, using user and listing IDs, and returns the result.
    @DELETE("/api/{userID}/{listingID}/unfavourite")
    fun deleteFavouriteListing(@Path("userID") userId: String, @Path("listingID") listingId: String): Call<UnfavouriteListingResponse>

    // Creates a new booking for a listing, including support documents and booking details, using multipart form data.
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

    // Retrieves the booking status and details for a specific booking by user ID.
    @GET("/api/bookings/{id}")
    fun getBookingById(@Path("id") userId: String): Call<BookingStatusResponse>

    // Submits a new maintenance request for a listing, including issue details and optional document uploads.
    @Multipart
    @POST("/api/{userID}/{listingID}/maintenance/request/create")
    fun createMaintenanceRequest(
        @Path("userID") userId: String,
        @Path("listingID") listingId: String,
        @Part("issue") issue: RequestBody,
        @Part("description") description: RequestBody,
        @Part("priority") priority: RequestBody,
        @Part documentURL: List<MultipartBody.Part>
    ) : Call<MaintenanceResponse>

    // Retrieves all maintenance requests submitted by a specific user.
    @GET("/api/{userID}/maintenance/request")
    fun getMaintenanceRequestForUser(@Path("userID") userId: String): Call<List<MaintenanceRequestResponse>>

    // Authenticates a user using Google OAuth credentials for mobile login.
    @POST("/auth/google/mobile")
    fun googleMobileLogin(@Body request: GoogleRequest): Call<GoogleResponse>

    // Submits a new review for a listing by a user, including review content and rating.
    @POST(value = "/api/reviews/{userID}/{listingID}/create")
    fun createReview(
        @Path("userID") userId: String,
        @Path("listingID") listingId: String,
        @Body request: CreateReviewRequest
    ): Call<ReviewResponse>

    // Updates user profile settings, including profile image and other details, using multipart form data.
    @Multipart
    @POST("/api/users/{id}/profile")
    fun updateUserSettings(
        @Path("id") userId: String,
        @Part settingParts: MutableList<MultipartBody.Part>
    ) : Call<UpdateSettingsResponse>

    // Retrieves a list of reviews for a specific listing.
    @GET("/api/{listingID}/reviews")
    fun getReviews(@Path("listingID") listingId: String): Call<List<ReviewResponse>>
}
