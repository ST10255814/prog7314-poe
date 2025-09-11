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
import com.example.rentwise.data_classes.FavouriteListingsResponse
import com.example.rentwise.data_classes.ListingResponse
import com.example.rentwise.databinding.FragmentWishListBinding
import com.example.rentwise.recyclerview_itemclick_views.PropertyDetails
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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




    }
    private fun getFavouriteListingsApiCall(){
        val api = RetrofitInstance.createAPIInstance(requireContext())
        api.getFavouriteListings().enqueue(object : Callback<List<FavouriteListingsResponse>> {
            override fun onResponse(
                call : Call<List<FavouriteListingsResponse>>,
                response : Response<List<FavouriteListingsResponse>>
            ) {
                if(!isAdded || _binding == null) return
                if(response.isSuccessful){
                    val favouriteList = response.body() ?: emptyList()
                    if(favouriteList.isNotEmpty()){
                        binding.wishlistRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                        binding.wishlistRecyclerView.adapter = WishlistAdapter(
                            wishlistProperties = favouriteList,
                            onItemClick = { selectedProperty ->
                                val intent = Intent(requireContext(), PropertyDetails::class.java)
                                intent.putExtra("property", selectedProperty)
                                startActivity(intent)
                            },
                            onUnFavouriteClick = {_, position ->
                                sampleList.removeAt(position)
                                binding.wishlistRecyclerView.adapter?.notifyItemRemoved(position)
                            }
                    }
                }
            }

            override fun onFailure(
                call : Call<List<FavouriteListingsResponse>>,
                t : Throwable
            ) {
                TODO("Not yet implemented")
            }

        })

    }
}