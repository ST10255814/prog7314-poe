package com.example.rentwise.home

import RetrofitInstance
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.rentwise.R
import com.example.rentwise.adapters.PropertyItemAdapter
import com.example.rentwise.auth.LoginActivity
import com.example.rentwise.custom_toast.CustomToast
import com.example.rentwise.data_classes.FavouriteListingsResponse
import com.example.rentwise.data_classes.ListingResponse
import com.example.rentwise.databinding.FragmentHomeBinding
import com.example.rentwise.recyclerview_itemclick_views.PropertyDetails
import com.example.rentwise.shared_pref_config.TokenManger
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(this)
            .load(R.drawable.temp_profile)
            .circleCrop()
            .into(binding.profileDisplay)

        updateDropdowns()
        setDropdownOnItemClicks()
        fetchListingsExcludingFavourites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateDropdowns() {
        if (!isAdded || _binding == null) return

        val locations = resources.getStringArray(R.array.location_options).toList()
        val rooms = resources.getStringArray(R.array.room_options).toList()
        val prices = resources.getStringArray(R.array.price_options).toList()

        val locationAdapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown_item, locations)
        val roomsAdapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown_item, rooms)
        val pricesAdapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown_item, prices)

        binding.dropdownLocation.setAdapter(locationAdapter)
        binding.dropdownPrices.setAdapter(pricesAdapter)
        binding.dropdownRooms.setAdapter(roomsAdapter)
    }

    private fun setDropdownOnItemClicks() {
        // Set initial text size for dropdowns
        binding.dropdownRooms.setTextSize(12.5f)

        // Increase text size on item selection
        binding.dropdownRooms.setOnItemClickListener { parent, view, position, id ->
            binding.dropdownRooms.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        }
        binding.dropdownLocation.setOnItemClickListener { parent, view, position, id ->
            binding.dropdownLocation.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            binding.searchLocationText.visibility = View.VISIBLE
            binding.locationIcon.visibility = View.VISIBLE
            binding.searchLocationText.text = binding.dropdownLocation.text
        }
        binding.dropdownPrices.setOnItemClickListener { parent, view, position, id ->
            binding.dropdownPrices.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        }
    }

    private fun fetchListingsExcludingFavourites() {
        showLoading()
        val api = RetrofitInstance.createAPIInstance(requireContext())
        val tokenManager = TokenManger(requireContext())
        val userId = tokenManager.getUser()

        if(userId != null) {
            api.getListings().enqueue( object : Callback<List<ListingResponse>> {
                override fun onResponse(
                    call: Call<List<ListingResponse>>,
                    response: Response<List<ListingResponse>>
                ) {
                    if(!isAdded || _binding == null) return
                    if(response.isSuccessful){
                        val allListings = response.body() ?: emptyList()

                        //Fetch favourites for filtering
                        api.getFavouriteListings(userId).enqueue( object : Callback<MutableList<FavouriteListingsResponse>> {
                            override fun onResponse(
                                call: Call<MutableList<FavouriteListingsResponse>>,
                                favResponse: Response<MutableList<FavouriteListingsResponse>>
                            ) {
                                if(!isAdded || _binding == null) return
                                if(favResponse.isSuccessful){
                                    hideLoading()
                                    //Extract favourite listing IDs
                                    val favouriteIds = favResponse.body()
                                        ?.mapNotNull { it.listingDetail?.listingID }
                                        ?.toSet() ?: emptySet()

                                    //Filter out favourites from all listings
                                    val nonFavouriteListings = allListings.filter { it.propertyId !in favouriteIds }

                                    //Pass filtered list to adapter and add onClick to each item
                                    val adapter = PropertyItemAdapter(nonFavouriteListings) { selected ->
                                        val intent = Intent(requireContext(), PropertyDetails::class.java)
                                        intent.putExtra("property", selected)
                                        startActivity(intent)
                                    }

                                    binding.propertiesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                                    binding.propertiesRecyclerView.adapter = adapter
                                    if (nonFavouriteListings.isEmpty()) {
                                        binding.propertiesRecyclerView.visibility = View.GONE
                                        binding.emptyView.emptyLayout.visibility = View.VISIBLE
                                    } else {
                                        binding.propertiesRecyclerView.visibility = View.VISIBLE
                                        binding.emptyView.emptyLayout.visibility = View.GONE
                                    }
                                    //Show success message
                                    CustomToast.show(requireContext(), "Listings loaded", CustomToast.Companion.ToastType.SUCCESS)
                                }
                                else{
                                    //Handle error response for favourites and show empty view
                                    binding.propertiesRecyclerView.visibility = View.GONE
                                    binding.emptyView.emptyLayout.visibility = View.VISIBLE
                                    val errorBody = response.errorBody()?.string()
                                    val errorMessage = if (errorBody != null) {
                                        try {
                                            val json = JSONObject(errorBody)
                                            json.getString("error")
                                        } catch (e: Exception) {
                                            "Unknown error"
                                        }
                                    } else {
                                        "Unknown error"
                                    }
                                    // Log out if unauthorized
                                    if (response.code() == 401) {
                                        tokenManager.clearToken()
                                        tokenManager.clearUser()

                                        val intent = Intent(requireContext(), LoginActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //Clear Activity trace
                                        startActivity(intent)
                                    }
                                    CustomToast.show(requireContext(), errorMessage, CustomToast.Companion.ToastType.ERROR)
                                }
                            }

                            override fun onFailure(
                                call: Call<MutableList<FavouriteListingsResponse>>,
                                t: Throwable
                            ) {
                                //Handle failure and show empty view
                                hideLoading()
                                if(!isAdded || _binding == null) return
                                binding.propertiesRecyclerView.visibility = View.GONE
                                binding.emptyView.emptyLayout.visibility = View.VISIBLE
                                CustomToast.show(requireContext(), t.message.toString(), CustomToast.Companion.ToastType.ERROR)
                                Log.e("Error", t.message.toString())
                            }

                        })
                    }
                    else{
                        //Handle listing error response and show empty view
                        binding.propertiesRecyclerView.visibility = View.GONE
                        binding.emptyView.emptyLayout.visibility = View.VISIBLE
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = if (errorBody != null) {
                            try {
                                val json = JSONObject(errorBody)
                                json.getString("error")
                            } catch (e: Exception) {
                                "Unknown error"
                            }
                        } else {
                            "Unknown error"
                        }
                        // Log out if unauthorized
                        if (response.code() == 401) {
                            tokenManager.clearToken()
                            tokenManager.clearUser()

                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //Clear Activity trace
                            startActivity(intent)
                        }
                        CustomToast.show(requireContext(), errorMessage, CustomToast.Companion.ToastType.ERROR)
                    }
                }

                override fun onFailure(
                    call: Call<List<ListingResponse>>,
                    t: Throwable
                ) {
                    //Handle failure and show empty view
                    hideLoading()
                    if(!isAdded || _binding == null) return
                    binding.propertiesRecyclerView.visibility = View.GONE
                    binding.emptyView.emptyLayout.visibility = View.VISIBLE
                    CustomToast.show(requireContext(), t.message.toString(), CustomToast.Companion.ToastType.ERROR)
                    Log.e("Error", t.message.toString())
                }
            })
        }
    }
    private fun showLoading() {
        binding.recyclerLoadingOverlay.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.recyclerLoadingOverlay.visibility = View.GONE
    }
}