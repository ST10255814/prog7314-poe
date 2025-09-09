package com.example.rentwise.wishlist

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentwise.R
import com.example.rentwise.adapters.WishlistAdapter
import com.example.rentwise.data_classes.ListingResponse
import com.example.rentwise.databinding.FragmentWishListBinding
import com.example.rentwise.recyclerview_itemclick_views.PropertyDetails

class WishlistFragment : Fragment() {
    private var _binding: FragmentWishListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**val sampleList = mutableListOf(
            ListingResponse(R.drawable.house_interior_temp, "The Aliso", "950 E 3rd St, Los Angeles, CA", "2 Rooms", "Heating", "R4,280", false),
            ListingResponse(R.drawable.house_interior_temp, "Sunset Villa", "123 Main St, LA", "4 Rooms", "Cooling", "R7,490", false),
            ListingResponse(R.drawable.house_interior_temp, "Hollywood Hills", "13 Sunset St, LA", "2 Rooms", "Wi-Fi", "R6,000", false),
            ListingResponse(R.drawable.house_interior_temp, "Beverly Hills", "1045 Casper St, LA", "5 Rooms", "Solar", "R15,000", false)
        )

        binding.wishlistRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.wishlistRecyclerView.adapter = WishlistAdapter(
            //wishlistProperties = sampleList,
            onItemClick = { selectedProperty ->
                val intent = Intent(requireContext(), PropertyDetails::class.java)
                intent.putExtra("property", selectedProperty)
                startActivity(intent)
            },
            onUnFavouriteClick = {_, position ->
                sampleList.removeAt(position)
                binding.wishlistRecyclerView.adapter?.notifyItemRemoved(position)
            }
        )**/
    }
}