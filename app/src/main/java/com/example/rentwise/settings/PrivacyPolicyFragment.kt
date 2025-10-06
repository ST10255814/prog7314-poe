package com.example.rentwise.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.rentwise.databinding.FragmentPrivacyPolicyBinding

// Fragment responsible for displaying the privacy policy content to the user.
// Utilizes view binding for safe and efficient access to layout views, and ensures proper cleanup to prevent memory leaks.
class PrivacyPolicyFragment : Fragment() {
    // Holds the binding instance for accessing the fragment's layout views.
    private var _binding: FragmentPrivacyPolicyBinding? = null
    private val binding get() = _binding!!

    // Inflates the privacy policy layout and initializes the binding for this fragment.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrivacyPolicyBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Cleans up the binding reference when the fragment is destroyed to avoid memory leaks.
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
