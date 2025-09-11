package com.example.rentwise.home

import RetrofitInstance
import android.content.Intent
import android.media.session.MediaSession.Token
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.rentwise.R
import com.example.rentwise.adapters.CustomSpinnerAdapter
import com.example.rentwise.adapters.PropertyItemAdapter
import com.example.rentwise.auth.LoginActivity
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

        updateSpinners()
        fetchListingsExcludingFavourites()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateSpinners() {
        if (!isAdded || _binding == null) return

        val locations = resources.getStringArray(R.array.location_options).toList()
        val rooms = resources.getStringArray(R.array.room_options).toList()
        val prices = resources.getStringArray(R.array.price_options).toList()

        val locationAdapter = CustomSpinnerAdapter(requireContext(), locations)
        val roomsAdapter = CustomSpinnerAdapter(requireContext(), rooms)
        val pricesAdapter = CustomSpinnerAdapter(requireContext(), prices)

        binding.spinnerRooms.adapter = roomsAdapter
        binding.spinnerLocation.adapter = locationAdapter
        binding.spinnerPrices.adapter = pricesAdapter
    }

    private fun fetchListingsExcludingFavourites() {
        val api = RetrofitInstance.createAPIInstance(requireContext())
        val tokenManager = TokenManger(requireContext())
        val userId = tokenManager.getUser() ?: return

        api.getListings().enqueue(object : Callback<List<ListingResponse>> {
            override fun onResponse(
                call: Call<List<ListingResponse>>,
                response: Response<List<ListingResponse>>
            ) {
                if (!isAdded || _binding == null) return
                if (!response.isSuccessful) return

                val allListings = response.body() ?: emptyList()

                //Fetch favourites
                api.getFavouriteListings(userId).enqueue(object : Callback<MutableList<FavouriteListingsResponse>> {
                    override fun onResponse(
                        call: Call<MutableList<FavouriteListingsResponse>>,
                        favResponse: Response<MutableList<FavouriteListingsResponse>>
                    ) {
                        if (!isAdded || _binding == null) return
                        if (!favResponse.isSuccessful) return

                        val favouriteIds = favResponse.body()
                            ?.mapNotNull { it.listingDetail?.listingID }
                            ?.toSet() ?: emptySet()

                        //Filter out favourites
                        val nonFavouriteListings = allListings.filter { it.propertyId !in favouriteIds }

                        //Pass filtered list to adapter
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
                    }

                    override fun onFailure(
                        call: Call<MutableList<FavouriteListingsResponse>>,
                        t: Throwable
                    ) {
                        Toast.makeText(requireContext(), "Failed to fetch favourites", Toast.LENGTH_SHORT).show()
                    }
                })
            }

            override fun onFailure(call: Call<List<ListingResponse>>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to fetch listings", Toast.LENGTH_SHORT).show()
            }
        })
    }
}