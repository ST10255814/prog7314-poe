package com.example.rentwise.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.rentwise.R
import com.example.rentwise.adapters.PropertyItemAdapter
import com.example.rentwise.data_classes.PropertyData
import com.example.rentwise.databinding.FragmentHomeBinding

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

        val locations = listOf("Areas","New York", "Los Angeles", "Chicago", "Houston")
        val roomOptions = listOf("Rooms", "Studio", "1 Room", "2 Rooms", "3+ Rooms")
        val priceOptions = listOf("Prices", "Any", "$500 - $1000", "$1000 - $1500", "$1500+")

        val sampleList = listOf(
            PropertyData(R.drawable.house_interior_temp, "The Aliso", "950 E 3rd St, Los Angeles, CA", "2 Rooms", "Heating", "R4,280"),
            PropertyData(R.drawable.house_interior_temp, "Sunset Villa", "123 Main St, LA", "4 Rooms", "Cooling", "R7,490"),
            PropertyData(R.drawable.house_interior_temp, "Hollywood Hills", "13 Sunset St, LA", "2 Rooms", "Wi-Fi", "R6 000"),
            PropertyData(R.drawable.house_interior_temp, "Beverly Hills", "1045 Casper St, LA", "5 Rooms", "Solar", "R15,000")
        )

        binding.propertiesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.propertiesRecyclerView.adapter = PropertyItemAdapter(sampleList)

        updateSpinner(locations, binding.spinnerLocation)
        updateSpinner(roomOptions, binding.spinnerRooms)
        updateSpinner(priceOptions, binding.spinnerPrices)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateSpinner(categories: List<String>, spinner: Spinner) {
        if (!isAdded || _binding == null) return
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }
}