package com.example.rentwise.wishlist

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
import com.example.rentwise.adapters.WishlistAdapter
import com.example.rentwise.auth.LoginActivity
import com.example.rentwise.data_classes.FavouriteListingsResponse
import com.example.rentwise.databinding.FragmentWishListBinding
import com.example.rentwise.recyclerview_itemclick_views.PropertyDetails
import com.example.rentwise.shared_pref_config.TokenManger
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WishlistFragment : Fragment() {
    private var _binding: FragmentWishListBinding? = null
    private val binding get() = _binding!!

    private lateinit var wishlistAdapter: WishlistAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWishListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getFavouriteListingsApiCall()
    }

    private fun getFavouriteListingsApiCall() {
        val tokenManger = TokenManger(requireContext())
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
                        val favouriteList = response.body()?.toMutableList() ?: mutableListOf()
                        if(favouriteList.isNotEmpty()){
                            binding.wishlistRecyclerView.visibility = View.VISIBLE
                            binding.emptyWishlistView.emptyLayout.visibility = View.GONE
                            wishlistAdapter = WishlistAdapter(
                                wishlistProperties = favouriteList,
                                onItemClick = { selectedItem ->
                                    val intent = Intent(requireContext(), PropertyDetails::class.java)
                                    intent.putExtra("property-wishList", selectedItem)
                                    startActivity(intent)
                                },
                                onUnFavouriteClick = {_, position ->
                                    wishlistAdapter.removeAt(position)
                                }
                            )
                            binding.wishlistRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                            binding.wishlistRecyclerView.adapter = wishlistAdapter
                        }
                        else{
                            binding.wishlistRecyclerView.visibility = View.GONE
                            binding.emptyWishlistView.emptyLayout.visibility = View.VISIBLE
                        }
                    }
                    else{
                        binding.wishlistRecyclerView.visibility = View.GONE
                        binding.emptyWishlistView.emptyLayout.visibility = View.VISIBLE

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
                        Log.e("Error", errorMessage)
                        // Log out if unauthorized
                        if (response.code() == 401) {
                            tokenManger.clearToken()
                            tokenManger.clearUser()

                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //Clear Activity trace
                            startActivity(intent)
                        }
                    }
                }
                override fun onFailure(
                    call: Call<MutableList<FavouriteListingsResponse>>,
                    t: Throwable
                ) {
                    if (!isAdded || _binding == null) return
                    binding.wishlistRecyclerView.visibility = View.GONE
                    binding.emptyWishlistView.emptyLayout.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("Error", t.message.toString())
                }
            })
        }
    }

    private fun deleteFavouriteItemFromDbApiCall(){

    }
}
