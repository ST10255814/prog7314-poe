package com.example.rentwise.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.rentwise.R
import com.example.rentwise.databinding.FragmentAboutBinding
import android.content.pm.PackageManager
import android.os.Build

// Fragment to display app information, version, credits, and quick links.
// Matches the visual style of other screens (cards, paddings, colors).
class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // App name + version from PackageManager for consistency across applicationId changes.
        binding.txtAppName.text = getString(R.string.app_name)
        binding.txtVersion.text = getString(R.string.about_version, getAppVersionName())

        // Optional: show package if you want (kept hidden by default).
        binding.txtPackage.isVisible = false // keep UI clean; toggle if needed.

        // Links:
        binding.cardPrivacy.setOnClickListener {
            // Delegate to your existing settings route by popping back to settings and tapping Privacy,
            // or simply open your policy URL if you have one. For now, open via URL placeholder.
            safeOpenUrl(getString(R.string.privacy_url))
        }
        binding.cardSupport.setOnClickListener {
            // Mailto intent to your support address.
            val email = getString(R.string.support_email)
            val mail = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email") // set instance property instead of Intent.setData
                putExtra(Intent.EXTRA_SUBJECT, "Support: ${getString(R.string.app_name)}")
            }
            try {
                startActivity(mail)
            } catch (_: ActivityNotFoundException) { /* ignore */ }
        }
        binding.cardWebsite.setOnClickListener {
            safeOpenUrl(getString(R.string.website_url))
        }
    }

    // Retrieves the app version name safely across API levels without relying on BuildConfig.
    @Suppress("DEPRECATION")
    private fun getAppVersionName(): String {
        return try {
            val pm = requireContext().packageManager
            val packageName = requireContext().packageName
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val pInfo = pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
                pInfo.versionName ?: "1.0"
            } else {
                val pInfo = pm.getPackageInfo(packageName, 0)
                pInfo.versionName ?: "1.0"
            }
        } catch (_: Exception) {
            "1.0"
        }
    }

    private fun safeOpenUrl(url: String) {
        if (url.isBlank()) return
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(intent)
        } catch (_: Exception) { /* ignore */ }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
