package com.example.rentwise.home

import RetrofitInstance
import android.content.Intent
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

        getListingsApiCall()
        updateSpinners()
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

    private fun getListingsApiCall(){
        val api = RetrofitInstance.createAPIInstance(requireContext())
        api.getListings().enqueue(object : Callback<List<ListingResponse>> {
            override fun onResponse(
                call: Call<List<ListingResponse>>,
                response: Response<List<ListingResponse>>
            ) {
                if (!isAdded || _binding == null) return
                if(response.isSuccessful) {
                    val propertyList = response.body() ?: emptyList()

                    if (propertyList.isNotEmpty()){
                        binding.propertiesRecyclerView.visibility = View.VISIBLE
                        binding.emptyView.emptyLayout.visibility = View.GONE

                        binding.propertiesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                        binding.propertiesRecyclerView.adapter = PropertyItemAdapter(propertyList) { selectedProperty ->
                            val intent = Intent(requireContext(), PropertyDetails::class.java)
                            intent.putExtra("property", selectedProperty)
                            startActivity(intent)
                        }
                        Toast.makeText(requireContext(), "Listings Loaded", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        binding.propertiesRecyclerView.visibility = View.GONE
                        binding.emptyView.emptyLayout.visibility = View.VISIBLE
                    }
                }
                else {
                    binding.propertiesRecyclerView.visibility = View.GONE
                    binding.emptyView.emptyLayout.visibility = View.VISIBLE

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
                            "Unknown error"
                        }
                    } else {
                        "Unknown error"
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    // Log out if unauthorized
                    val tokenManger = TokenManger(requireContext())
                    if (response.code() == 401) {
                        tokenManger.clearToken()
                        tokenManger.clearUser()

                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //Clear Activity trace
                        startActivity(intent)
                    }
                }
            }

            override fun onFailure(call: Call<List<ListingResponse>>, t: Throwable) {
                if (!isAdded || _binding == null) return
                binding.propertiesRecyclerView.visibility = View.GONE
                binding.emptyView.emptyLayout.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("Home Screen", "Error: ${t.message.toString()}")
            }
        })
    }
}