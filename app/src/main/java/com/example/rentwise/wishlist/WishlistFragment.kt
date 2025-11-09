package com.example.rentwise.wishlist

import android.content.Context
import com.example.rentwise.retrofit_instance.RetrofitInstance
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentwise.R
import com.example.rentwise.adapters.WishlistAdapter
import com.example.rentwise.auth.LoginActivity
import com.example.rentwise.custom_toast.CustomToast
import com.example.rentwise.data_classes.FavouriteListingsResponse
import com.example.rentwise.data_classes.UnfavouriteListingResponse
import com.example.rentwise.databinding.FragmentWishListBinding
import com.example.rentwise.recyclerview_itemclick_views.PropertyDetails
import com.example.rentwise.shared_pref_config.TokenManger
import com.example.rentwise.utils.LocaleHelper
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Fragment responsible for displaying and managing the user's wishlist.
class WishlistFragment : Fragment() {
    // View binding for accessing layout views.
    private var _binding: FragmentWishListBinding? = null
    private val binding get() = _binding!!
    // Token manager for user authentication and session management.
    private lateinit var tokenManger: TokenManger
    // Adapter for the wishlist RecyclerView.
    private lateinit var wishlistAdapter: WishlistAdapter

    // OVERRIDE ONATTACH TO APPLY SAVED LOCALE
    // This ensures the saved language is applied when the fragment is attached
    override fun onAttach(context: Context) {
        super.onAttach(LocaleHelper.onAttach(context))
    }

    // Inflate the fragment layout and initialize view binding.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishListBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Clean up binding to prevent memory leaks.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Called after the view is created; initializes token manager and fetches wishlist data.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManger = TokenManger(requireContext())
        getFavouriteListingsApiCall()
    }

    // Fetches the user's favourite listings from the backend API.
    private fun getFavouriteListingsApiCall() {
        showOverlay()
        val userId = tokenManger.getUser()
        if(userId != null){
            val api = RetrofitInstance.createAPIInstance(requireContext())
            api.getFavouriteListings(userId).enqueue( object: Callback<MutableList<FavouriteListingsResponse>> {
                override fun onResponse(
                    call: Call<MutableList<FavouriteListingsResponse>>,
                    response: Response<MutableList<FavouriteListingsResponse>>
                ) {
                    if(!isAdded || _binding == null) return
                    if(response.isSuccessful){
                        hideOverlay()
                        showRecyclerView()
                        val favouriteList = response.body()?.toMutableList() ?: mutableListOf()
                        if(favouriteList.isNotEmpty()){
                            // Set up the adapter with click listeners for item selection and unfavourite actions.
                            wishlistAdapter = WishlistAdapter(
                                wishlistProperties = favouriteList,
                                onItemClick = { selectedItem ->
                                    // Open property details when an item is clicked.
                                    val intent = Intent(requireContext(), PropertyDetails::class.java)
                                    intent.putExtra("property-wishList", selectedItem)
                                    startActivity(intent)
                                },
                                onUnFavouriteClick = {_, position ->
                                    // Remove item from wishlist when unfavourite is clicked.
                                    val listingId = favouriteList[position].listingDetail?.listingID //get the listing ID of the selected position
                                    deleteFavouriteItemFromDbApiCall(listingId, position)
                                }
                            )
                            binding.wishlistRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                            binding.wishlistRecyclerView.adapter = wishlistAdapter

                            CustomToast.show(requireContext(), "Wishlist loaded", CustomToast.Companion.ToastType.SUCCESS)
                        }
                        else{
                            showEmptyRecyclerView()
                        }
                    }
                    else{
                        hideOverlay()
                        showEmptyRecyclerView()
                        // Parse and display error message from API response.
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = if (errorBody != null) {
                            try {
                                val json = JSONObject(errorBody)
                                when {
                                    json.has("message") -> json.getString("message")
                                    json.has("error") -> json.getString("error")
                                    else -> "Unknown error"
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                "Unknown error"
                            }
                        } else {
                            "Unknown error"
                        }
                        CustomToast.show(requireContext(), errorMessage, CustomToast.Companion.ToastType.ERROR)
                        Log.e("Error", errorMessage)
                        // Log out if unauthorized
                        if (response.code() == 401) {
                            tokenManger.clearToken()
                            tokenManger.clearUser()
                            tokenManger.clearPfp()

                            CustomToast.show(requireContext(), getString(R.string.session_expired_message),
                                CustomToast.Companion.ToastType.ERROR)
                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //Clear Activity trace
                            startActivity(intent)
                        }
                    }
                }
                // Handles network or other failures during the API call.
                override fun onFailure(
                    call: Call<MutableList<FavouriteListingsResponse>>,
                    t: Throwable
                ) {
                    hideOverlay()
                    if (!isAdded || _binding == null) return
                    showEmptyRecyclerView()
                    CustomToast.show(requireContext(), "${t.message}", CustomToast.Companion.ToastType.ERROR)
                    Log.e("Error", t.message.toString())
                }
            })
        }
    }

    // Calls the API to remove a listing from the user's favourites.
    private fun deleteFavouriteItemFromDbApiCall(listingId: String?, position: Int){
        showUnfavouriteOverlay()
        val userId = tokenManger.getUser()

        val api = RetrofitInstance.createAPIInstance(requireContext())
        if(userId != null && listingId != null){
            api.deleteFavouriteListing(userId, listingId).enqueue(object : Callback<UnfavouriteListingResponse> {
                override fun onResponse(
                    call: Call<UnfavouriteListingResponse>,
                    response: Response<UnfavouriteListingResponse>
                ) {
                    if (!isAdded || _binding == null) return // Fragment not attached to context
                    if (response.isSuccessful) {
                        hideUnfavouriteOverlay()
                        val responseBody = response.body()
                        if (responseBody != null) {
                            // Successfully unfavourited
                            responseBody.message?.let {
                                CustomToast.show(requireContext(), it, CustomToast.Companion.ToastType.SUCCESS)
                            }
                            wishlistAdapter.removeAt(position)
                            if(wishlistAdapter.itemCount == 0) {
                                showEmptyRecyclerView()
                            }
                        }
                    }
                    else{
                        // Handle error response
                        hideUnfavouriteOverlay()
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = if (errorBody != null) {
                            try {
                                val json = JSONObject(errorBody)
                                when { // Check for both "message" and "error" keys
                                    json.has("message") -> json.getString("message")
                                    json.has("error") -> json.getString("error")
                                    else -> "Unknown error"
                                }
                            } catch (e: Exception) {
                                "Unknown error"
                            }
                        } else {
                            "Unknown error"
                        }
                        CustomToast.show(requireContext(), errorMessage, CustomToast.Companion.ToastType.ERROR)
                        Log.e("Error", errorMessage)
                        // Log out if unauthorized
                        if (response.code() == 401) {
                            tokenManger.clearToken()
                            tokenManger.clearUser()
                            tokenManger.clearPfp()

                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //Clear Activity trace
                            startActivity(intent)
                        }
                    }
                }
                // Handles network or other failures during the API call.
                override fun onFailure(
                    call: Call<UnfavouriteListingResponse>,
                    t: Throwable
                ) {
                    hideUnfavouriteOverlay()
                    if (!isAdded || _binding == null) return
                    showEmptyRecyclerView()
                    CustomToast.show(requireContext(), "Error: ${t.message.toString()}", CustomToast.Companion.ToastType.ERROR)
                    Log.e("Error", t.message.toString())
                }
            })
        }
    }

    // Shows a loading overlay while fetching wishlist data.
    private fun showOverlay() {
        binding.wishlistLoadingOverlay.visibility = View.VISIBLE
    }
    // Hides the loading overlay.
    private fun hideOverlay() {
        binding.wishlistLoadingOverlay.visibility = View.GONE
    }
    // Shows an overlay while unfavouriting an item.
    private fun showUnfavouriteOverlay() {
        binding.unfavouriteOverlay.visibility = View.VISIBLE
    }
    // Hides the unfavourite overlay.
    private fun hideUnfavouriteOverlay() {
        binding.unfavouriteOverlay.visibility = View.GONE
    }
    // Displays the empty state view when there are no wishlist items.
    private fun showEmptyRecyclerView(){
        binding.wishlistRecyclerView.visibility = View.GONE
        binding.emptyWishlistView.emptyLayout.visibility = View.VISIBLE
    }
    // Displays the wishlist RecyclerView when there are items.
    private fun showRecyclerView(){
        binding.wishlistRecyclerView.visibility = View.VISIBLE
        binding.emptyWishlistView.emptyLayout.visibility = View.GONE
    }
}
