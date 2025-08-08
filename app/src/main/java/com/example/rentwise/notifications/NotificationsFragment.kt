package com.example.rentwise.notifications

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentwise.NotificationAdapter
import com.example.rentwise.R
import com.example.rentwise.data_classes.NotificationData
import com.example.rentwise.databinding.FragmentNotificationsBinding
import java.sql.Time

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

        val now = { Time(System.currentTimeMillis()) }

        val notifications = listOf(
            NotificationData("New Property Alert", "A luxury apartment in Cape Town is now available.", now()),
            NotificationData("Price Drop", "The apartment you liked has dropped in price.", now()),
            NotificationData("New Message", "You have a message from the property owner.", now()),
            NotificationData("Viewing Reminder", "You have a viewing scheduled for tomorrow at 3 PM.", now())
        )

        binding.wishlistRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.wishlistRecyclerView.adapter = NotificationAdapter(notifications)
    }
}