package com.example.rentwise.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.rentwise.R
import com.example.rentwise.adapters.CustomSpinnerAdapter
import com.example.rentwise.adapters.PropertyItemAdapter
import com.example.rentwise.data_classes.PropertyData
import com.example.rentwise.databinding.FragmentHomeBinding
import com.example.rentwise.recyclerview_itemclick_views.PropertyDetails

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

        val sampleList = listOf(
            PropertyData(R.drawable.house_interior_temp, "The Aliso", "950 E 3rd St, Los Angeles, CA", "2 Rooms", "Heating", "R4,280", false),
            PropertyData(R.drawable.house_interior_temp, "Sunset Villa", "123 Main St, LA", "4 Rooms", "Cooling", "R7,490", false),
            PropertyData(R.drawable.house_interior_temp, "Hollywood Hills", "13 Sunset St, LA", "2 Rooms", "Wi-Fi", "R6,000", false),
            PropertyData(R.drawable.house_interior_temp, "Beverly Hills", "1045 Casper St, LA", "5 Rooms", "Solar", "R15,000", false)
        )

        binding.propertiesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.propertiesRecyclerView.adapter = PropertyItemAdapter(sampleList) { selectedProperty ->

            val intent = Intent(requireContext(), PropertyDetails::class.java)
            intent.putExtra("property", selectedProperty)
            startActivity(intent)
        }

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
}