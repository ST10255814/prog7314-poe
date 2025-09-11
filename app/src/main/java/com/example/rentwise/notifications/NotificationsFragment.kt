package com.example.rentwise.notifications

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentwise.NotificationAdapter
import com.example.rentwise.auth.LoginActivity
import com.example.rentwise.data_classes.NotificationResponse
import com.example.rentwise.databinding.FragmentNotificationsBinding
import com.example.rentwise.shared_pref_config.TokenManger
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchNotifications()
    }

    private fun fetchNotifications() {
        val api = RetrofitInstance.createAPIInstance(requireContext())
        api.getNotifications().enqueue(object : Callback<List<NotificationResponse>> {
            override fun onResponse(
                call: Call<List<NotificationResponse>>,
                response: Response<List<NotificationResponse>>
            ) {
                if (!isAdded || _binding == null) return
                if (response.isSuccessful) {
                    val body = response.body() ?: emptyList()
                    if (body.isNotEmpty()) {
                        val adapter = NotificationAdapter(
                            notifications = body
                        )
                        binding.wishlistRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                        binding.wishlistRecyclerView.adapter = adapter
                        Toast.makeText(requireContext(), "Notifications fetched successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "No notifications found", Toast.LENGTH_SHORT).show()
                    }
                }
                else {
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

            override fun onFailure(call: Call<List<NotificationResponse>>, t: Throwable) {
                if (!isAdded || _binding == null) return
                Toast.makeText(requireContext(), "Error: ${t.message.toString()}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
