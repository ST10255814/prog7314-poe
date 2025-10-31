package com.example.rentwise.settings

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.rentwise.databinding.FragmentPrivacyPolicyBinding
import com.example.rentwise.utils.LocaleHelper

// Fragment responsible for displaying the privacy policy content to the user.
// Utilizes view binding for safe and efficient access to layout views, and ensures proper cleanup to prevent memory leaks.
class PrivacyPolicyFragment : Fragment() {
    // Holds the binding instance for accessing the fragment's layout views.
    private var _binding: FragmentPrivacyPolicyBinding? = null
    private val binding get() = _binding!!

    // This ensures the saved language is applied when the fragment is attached
    override fun onAttach(context: Context) {
        super.onAttach(LocaleHelper.onAttach(context))
    }

    // Inflates the privacy policy layout and initializes the binding for this fragment.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrivacyPolicyBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Render HTML (bold + links) from strings.xml
        val raw = resources.getString(com.example.rentwise.R.string.privacy_policy)

        val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(raw, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(raw)
        }

        binding.privacyText.text = spanned
        binding.privacyText.movementMethod = LinkMovementMethod.getInstance()
    }

    // Cleans up the binding reference when the fragment's view is destroyed to avoid memory leaks.
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

