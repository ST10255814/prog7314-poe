package com.example.rentwise.home

import com.example.rentwise.retrofit_instance.RetrofitInstance
import android.annotation.SuppressLint
import android.content.Context
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
import com.example.rentwise.utils.LocaleHelper
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Fragment responsible for displaying the home screen, including property listings, filters, and user profile image.
class HomeFragment : Fragment() {
    // Holds the binding for accessing layout views.
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    // Stores the list of property listings after filtering out favourites.
    private var filteredListings: List<ListingResponse> = emptyList()
    // Adapter for displaying property items in the RecyclerView.
    private lateinit var adapter: PropertyItemAdapter
    // Manages user authentication tokens and profile image.
    private lateinit var tokenManger: TokenManger

    // OVERRIDE ONATTACH TO APPLY SAVED LOCALE
    // This ensures the saved language is applied when the fragment is attached
    override fun onAttach(context: Context) {
        super.onAttach(LocaleHelper.onAttach(context))
    }

    // Inflates the layout and initializes the binding for the fragment.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Sets up the UI, loads the profile image, initializes filters, and fetches property listings.
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

        updateDropdowns() // Populates filter dropdowns with options.
        setUpPropertyAdapter() // Configures the RecyclerView adapter for property listings.
        setFilterClicks() // Attaches listeners for filter and search interactions.
        fetchListingsExcludingFavourites() // Loads property listings, excluding user's favourites.
    }

    // Cleans up the binding to prevent memory leaks when the view is destroyed.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Initializes the property adapter and sets up item click navigation to property details.
    private fun setUpPropertyAdapter(){
        adapter = PropertyItemAdapter(filteredListings) { selected ->
            val intent = Intent(requireContext(), PropertyDetails::class.java)
            intent.putExtra("property", selected)
            startActivity(intent)
        }
        binding.propertiesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.propertiesRecyclerView.adapter = adapter
    }

    // Populates the location and price dropdowns with predefined options and sets default selections.
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
    // Attaches listeners to filter dropdowns and search view to trigger property filtering.
    private fun setFilterClicks() {
        binding.dropdownLocation.setOnItemClickListener { _, _, _, _ ->
            binding.dropdownLocation.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            // Updates the displayed location filter text.
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

        // Listens for search query changes and applies filters accordingly.
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

    // Applies location, price, and search text filters to the property listings and updates the UI.
    private fun applyListingFilters(){
        if(!isAdded || _binding == null) return
        showFilterLoading()

        val selectedLocation = binding.dropdownLocation.text.toString().trim()
        val selectedPrice = binding.dropdownPrices.text.toString().trim()
        val searchText = binding.searchView.query?.toString()?.trim()?.lowercase() ?: ""

        var userFilteredList = filteredListings

        // Filters listings by selected location if not "All".
        if (selectedLocation.isNotEmpty() && !selectedLocation.equals("All", ignoreCase = true)) {
            userFilteredList = userFilteredList.filter { listing ->
                // now also checks listing.area in addition to address
                val areaMatch = listing.area?.contains(selectedLocation, ignoreCase = true) == true
                val addressMatch = listing.address?.contains(selectedLocation, ignoreCase = true) == true
                areaMatch || addressMatch
            }
        }
        // Filters listings by selected price range.
        if (selectedPrice.isNotEmpty() && !selectedPrice.equals("Any", ignoreCase = true)) {
            userFilteredList = when {
                selectedPrice.contains("R0-R5k", ignoreCase = true) -> {
                    val limit = 5000f
                    userFilteredList.filter { (it.price ?: 0f) <= limit }
                }
                selectedPrice.contains("R5k-R10k", ignoreCase = true) -> {
                    val lowerLimit =  5000f
                    val upperLimit = 10000f
                    userFilteredList.filter { (it.price ?: 0f) in lowerLimit..upperLimit }
                }
                selectedPrice.contains("R10k+", ignoreCase = true) -> {
                    val limit = 10000f
                    userFilteredList.filter { (it.price ?: 0f) >= limit }
                }
                else -> userFilteredList
            }
        }

        // Filters listings by search text across multiple fields.
        if (searchText.isNotEmpty()) {
            userFilteredList = userFilteredList.filter { listing ->
                val matchesTitle = listing.title?.contains(searchText, ignoreCase = true) == true
                val matchesAddress = listing.address?.contains(searchText, ignoreCase = true) == true
                val matchesArea = listing.area?.contains(searchText, ignoreCase = true) == true
                val matchesDescription = listing.description?.contains(searchText, ignoreCase = true) == true
                val matchesAmenities = listing.amenities?.any { amenity ->
                    amenity.contains(searchText, ignoreCase = true)
                } == true

                matchesTitle || matchesAddress || matchesArea || matchesDescription || matchesAmenities
            }
        }
        adapter.updateListViaFilters(userFilteredList)

        // Displays an empty view if no listings match the filters.
        if (userFilteredList.isEmpty()) {
            binding.propertiesRecyclerView.visibility = View.GONE
            binding.emptyView.emptyLayout.visibility = View.VISIBLE
        } else {
            binding.propertiesRecyclerView.visibility = View.VISIBLE
            binding.emptyView.emptyLayout.visibility = View.GONE
        }

        hideFilterLoading()
    }

    // Fetches all property listings, removes those in the user's favourites, and updates the UI.
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

                        // Fetches the user's favourite listings to exclude them from the main list.
                        api.getFavouriteListings(userId).enqueue( object : Callback<MutableList<FavouriteListingsResponse>> {
                            override fun onResponse(
                                call: Call<MutableList<FavouriteListingsResponse>>,
                                favResponse: Response<MutableList<FavouriteListingsResponse>>
                            ) {
                                if(!isAdded || _binding == null) return
                                if(favResponse.isSuccessful){
                                    hideLoading()
                                    val favouriteIds = favResponse.body()
                                        ?.mapNotNull { it.listingDetail?.listingID }
                                        ?.toSet() ?: emptySet()

                                    filteredListings = allListings.filter { it.propertyId !in favouriteIds }

                                    adapter.updateListViaFilters(filteredListings)

                                    if (filteredListings.isEmpty()) {
                                        binding.propertiesRecyclerView.visibility = View.GONE
                                        binding.emptyView.emptyLayout.visibility = View.VISIBLE
                                    } else {
                                        CustomToast.show(requireContext(), "Listings loaded", CustomToast.Companion.ToastType.SUCCESS)
                                        binding.propertiesRecyclerView.visibility = View.VISIBLE
                                        binding.emptyView.emptyLayout.visibility = View.GONE
                                    }
                                }
                                else{
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
                                    if (response.code() == 401) {
                                        tokenManger.clearToken()
                                        tokenManger.clearUser()
                                        tokenManger.clearPfp()

                                        CustomToast.show(requireContext(), getString(R.string.session_expired_message),
                                            CustomToast.Companion.ToastType.ERROR)
                                        val intent = Intent(requireContext(), LoginActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                    }
                                    CustomToast.show(requireContext(), errorMessage, CustomToast.Companion.ToastType.ERROR)
                                }
                            }

                            override fun onFailure(
                                call: Call<MutableList<FavouriteListingsResponse>>,
                                t: Throwable
                            ) {
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
                        if (response.code() == 401) {
                            tokenManger.clearToken()
                            tokenManger.clearUser()
                            tokenManger.clearPfp()

                            CustomToast.show(requireContext(), getString(R.string.session_expired_message),
                                CustomToast.Companion.ToastType.ERROR)
                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        CustomToast.show(requireContext(), errorMessage, CustomToast.Companion.ToastType.ERROR)
                    }
                }

                override fun onFailure(
                    call: Call<List<ListingResponse>>,
                    t: Throwable
                ) {
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

    // Displays a loading overlay while property listings are being fetched.
    private fun showLoading() {
        binding.recyclerLoadingOverlay.visibility = View.VISIBLE
    }

    // Hides the loading overlay after property listings are loaded.
    private fun hideLoading() {
        binding.recyclerLoadingOverlay.visibility = View.GONE
    }

    // Shows a loading overlay when filters are being applied.
    private fun showFilterLoading() {
        binding.filterLoadingOverlay.visibility = View.VISIBLE
    }

    // Hides the filter loading overlay after filtering is complete.
    private fun hideFilterLoading() {
        binding.filterLoadingOverlay.visibility = View.GONE
    }
}
