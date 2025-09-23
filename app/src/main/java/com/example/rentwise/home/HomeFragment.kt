package com.example.rentwise.home

import RetrofitInstance
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
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
    private var filteredListings: List<ListingResponse> = emptyList()
    private lateinit var adapter: PropertyItemAdapter
    private lateinit var tokenManger: TokenManger

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManger = TokenManger(requireContext())

        val pfpImage = tokenManger.getPfp()
        Log.d("Pfp Image", pfpImage.toString())
        if(pfpImage != null) {
            Glide.with(this)
                .load(pfpImage)
                .circleCrop()
                .error(R.drawable.ic_empty)
                .placeholder(R.drawable.ic_empty)
                .into(binding.profileDisplay)
        }
        else Glide.with(this)
                .load(R.drawable.profile_icon)
                .circleCrop()
                .error(R.drawable.ic_empty)
                .placeholder(R.drawable.ic_empty)
                .into(binding.profileDisplay)


        updateDropdowns()
        setUpPropertyAdapter()
        setFilterClicks()
        fetchListingsExcludingFavourites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpPropertyAdapter(){
        adapter = PropertyItemAdapter(filteredListings) { selected ->
            val intent = Intent(requireContext(), PropertyDetails::class.java)
            intent.putExtra("property", selected)
            startActivity(intent)
        }
        binding.propertiesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.propertiesRecyclerView.adapter = adapter
    }

    private fun updateDropdowns() {
        if (!isAdded || _binding == null) return

        val locations = resources.getStringArray(R.array.location_options).toList()
        val prices = resources.getStringArray(R.array.price_options).toList()

        val locationAdapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown_item, locations)
        val pricesAdapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown_item, prices)

        binding.dropdownLocation.setAdapter(locationAdapter)
        binding.dropdownPrices.setAdapter(pricesAdapter)

        binding.dropdownLocation.setText(locations[0], false)
        binding.dropdownPrices.setText(prices[0], false)
    }

    @SuppressLint("SetTextI18n")
    private fun setFilterClicks() {
        binding.dropdownLocation.setOnItemClickListener { _, _, _, _ ->
            binding.dropdownLocation.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            // Update searchLocationText with selected location
            if(binding.dropdownLocation.text.toString().trim() != "All"){
                binding.searchLocationText.text = binding.dropdownLocation.text
            }
            else{
                binding.searchLocationText.text = "South Africa"
            }
            applyListingFilters()
        }
        binding.dropdownPrices.setOnItemClickListener { _, _, _, _ ->
            binding.dropdownPrices.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            applyListingFilters()
        }

        // Search view listener for on text change and submission
        binding.searchView.setOnQueryTextListener ( object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                applyListingFilters()
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                applyListingFilters()
                return true
            }
        })
    }

    private fun applyListingFilters(){
        if(!isAdded || _binding == null) return
        showFilterLoading()

        //Get filter values
        val selectedLocation = binding.dropdownLocation.text.toString().trim()
        val selectedPrice = binding.dropdownPrices.text.toString().trim()
        val searchText = binding.searchView.query?.toString()?.trim()?.lowercase() ?: "" //Can be empty

        //Apply filters and update recycler view
        var userFilteredList = filteredListings // Start with the full list

        //Location filter
        if (selectedLocation.isNotEmpty() && !selectedLocation.equals("All", ignoreCase = true)) {
            userFilteredList = userFilteredList.filter { listing ->
                listing.address?.contains(selectedLocation, ignoreCase = true) == true
            }
        }
        //Price filter
        // Filter price below 5k, between 5k-10k and above 10k
        if (selectedPrice.isNotEmpty() && !selectedPrice.equals("Any", ignoreCase = true)) {
            userFilteredList = when {
                selectedPrice.contains("Below", ignoreCase = true) -> {
                    val limit = 5000f
                    userFilteredList.filter { (it.price ?: 0f) < limit }
                }
                selectedPrice.contains("R5k-R10k", ignoreCase = true) -> {
                    val lowerLimit =  5000f
                    val upperLimit = 10000f
                    userFilteredList.filter { (it.price ?: 0f) in lowerLimit..upperLimit }
                }
                selectedPrice.contains("Above", ignoreCase = true) -> {
                    val limit = 10000f
                    userFilteredList.filter { (it.price ?: 0f) > limit }
                }
                else -> userFilteredList // If "Any" or unrecognized value is entered
            }
        }

        //Search text filter
        if (searchText.isNotEmpty()) {
            userFilteredList = userFilteredList.filter { listing ->
                //Check that title, address, description and amenities matches the search text individually
                val matchesTitle = listing.title?.contains(searchText, ignoreCase = true) == true
                val matchesAddress = listing.address?.contains(searchText, ignoreCase = true) == true
                val matchesDescription = listing.description?.contains(searchText, ignoreCase = true) == true
                val matchesAmenities = listing.amenities?.any { amenity ->
                    amenity.contains(searchText, ignoreCase = true)
                } == true

                matchesTitle || matchesAddress || matchesDescription || matchesAmenities // Return conditions to filter the list by (either or)
            }
        }
        //Update adapter with filtered list
        adapter.updateListViaFilters(userFilteredList)

        //Show empty view if no results
        if (userFilteredList.isEmpty()) {
            binding.propertiesRecyclerView.visibility = View.GONE
            binding.emptyView.emptyLayout.visibility = View.VISIBLE
        } else {
            binding.propertiesRecyclerView.visibility = View.VISIBLE
            binding.emptyView.emptyLayout.visibility = View.GONE
        }

        hideFilterLoading()
    }

    private fun fetchListingsExcludingFavourites() {
        showLoading()
        val api = RetrofitInstance.createAPIInstance(requireContext())
        val userId = tokenManger.getUser()

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
                                    filteredListings = allListings.filter { it.propertyId !in favouriteIds }

                                    //Pass filtered list to adapter and add onClick to each item
                                    adapter.updateListViaFilters(filteredListings)

                                    if (filteredListings.isEmpty()) {
                                        binding.propertiesRecyclerView.visibility = View.GONE
                                        binding.emptyView.emptyLayout.visibility = View.VISIBLE
                                    } else {
                                        //Show success message
                                        CustomToast.show(requireContext(), "Listings loaded", CustomToast.Companion.ToastType.SUCCESS)
                                        binding.propertiesRecyclerView.visibility = View.VISIBLE
                                        binding.emptyView.emptyLayout.visibility = View.GONE
                                    }
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
                                        tokenManger.clearToken()
                                        tokenManger.clearUser()
                                        tokenManger.clearPfp()

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
                            tokenManger.clearToken()
                            tokenManger.clearUser()
                            tokenManger.clearPfp()

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

    private fun showFilterLoading() {
        binding.filterLoadingOverlay.visibility = View.VISIBLE
    }

    private fun hideFilterLoading() {
        binding.filterLoadingOverlay.visibility = View.GONE
    }
}