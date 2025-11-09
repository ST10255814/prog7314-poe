package com.example.rentwise.notifications

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentwise.R
import com.example.rentwise.adapters.NotificationAdapter
import com.example.rentwise.auth.LoginActivity
import com.example.rentwise.custom_toast.CustomToast
import com.example.rentwise.data_classes.NotificationResponse
import com.example.rentwise.databinding.FragmentNotificationsBinding
import com.example.rentwise.retrofit_instance.RetrofitInstance
import com.example.rentwise.shared_pref_config.TokenManger
import com.example.rentwise.utils.LocaleHelper
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Fragment responsible for displaying user notifications, handling API calls, UI states, and authentication errors.
class NotificationsFragment : Fragment() {
    // Holds the binding for accessing layout views.
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
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
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Cleans up the binding to prevent memory leaks when the fragment is destroyed.
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // Sets up the UI and initiates fetching notifications after the view is created.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tokenManger = TokenManger(requireContext())
        fetchNotifications()
    }

    // Fetches notifications from the backend, updates the UI, and handles errors or authentication issues.
    private fun fetchNotifications() {
        showOverlay() // Displays a loading overlay while fetching data.
        val userId = tokenManger.getUser() ?: return
        val api = RetrofitInstance.createAPIInstance(requireContext())
        api.getUserNotifications(userId).enqueue(object : Callback<List<NotificationResponse>> {
            override fun onResponse(
                call: Call<List<NotificationResponse>>,
                response: Response<List<NotificationResponse>>
            ) {
                if (!isAdded || _binding == null) return
                if (response.isSuccessful) {
                    hideOverlay()
                    val body = response.body() ?: emptyList()
                    if (body.isNotEmpty()) {
                        showRecyclerView() // Shows the RecyclerView with notifications.
                        val adapter = NotificationAdapter(
                            notifications = body
                        )
                        binding.notificationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                        binding.notificationRecyclerView.adapter = adapter
                        CustomToast.show(requireContext(), "Notifications loaded", CustomToast.Companion.ToastType.INFO)
                    } else {
                        showEmptyRecyclerView() // Shows an empty view if there are no notifications.
                    }
                }
                else {
                    hideOverlay()
                    showEmptyRecyclerView()
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
                    CustomToast.show(requireContext(), errorMessage, CustomToast.Companion.ToastType.ERROR)
                    Log.e("Error", errorMessage)
                    // Handles authentication errors by clearing session and redirecting to login.
                    val tokenManger = TokenManger(requireContext())
                    if (response.code() == 401) {
                        tokenManger.clearToken()
                        tokenManger.clearUser()

                        CustomToast.show(requireContext(), getString(R.string.session_expired_message),
                            CustomToast.Companion.ToastType.ERROR)
                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                }
            }

            // Handles network or unexpected errors, displaying an error message.
            override fun onFailure(call: Call<List<NotificationResponse>>, t: Throwable) {
                if (!isAdded || _binding == null) return
                hideOverlay()
                showEmptyRecyclerView()
                CustomToast.show(requireContext(), "Error: ${t.message.toString()}", CustomToast.Companion.ToastType.ERROR)
            }
        })
    }

    // Displays a loading overlay to indicate that data is being fetched.
    private fun showOverlay() {
        binding.recyclerViewLoadingOverlay.visibility = View.VISIBLE
    }

    // Hides the loading overlay after data fetching is complete.
    private fun hideOverlay() {
        binding.recyclerViewLoadingOverlay.visibility = View.GONE
    }

    // Shows the RecyclerView with notifications and hides the empty view.
    private fun showRecyclerView(){
        binding.notificationRecyclerView.visibility = View.VISIBLE
        binding.emptyNotificationView.emptyLayout.visibility = View.GONE
    }

    // Shows an empty view when there are no notifications to display.
    private fun showEmptyRecyclerView(){
        binding.notificationRecyclerView.visibility = View.GONE
        binding.emptyNotificationView.emptyLayout.visibility = View.VISIBLE
    }
}
