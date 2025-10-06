package com.example.rentwise.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.rentwise.R
import com.example.rentwise.auth.LoginActivity
import com.example.rentwise.custom_toast.CustomToast
import com.example.rentwise.data_classes.UpdateSettingsResponse
import com.example.rentwise.data_classes.UserSettingsResponse
import com.example.rentwise.databinding.FragmentMainSettingsBinding
import com.example.rentwise.faq.FAQChatBot
import com.example.rentwise.retrofit_instance.RetrofitInstance
import com.example.rentwise.shared_pref_config.TokenManger
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Fragment for managing user settings, including language, notifications, offline sync, and navigation to support/about/privacy sections.
class MainSettingsFragment : Fragment() {
    // Binds the layout for the settings screen, providing access to all UI elements.
    private var _binding: FragmentMainSettingsBinding? = null
    private val binding get() = _binding!!
    // Stores multipart form data for updating user settings.
    private val parts = mutableListOf<MultipartBody.Part>()
    // Tracks whether the UI is initializing to prevent unwanted updates.
    private var isInitializing = true

    // Maps language codes to their display names for spinner selection and backend communication.
    val languageMap = mapOf(
        "en" to "English",
        "af" to "Afrikaans",
        "st" to "Sotho",
        "zu" to "Zulu"
    )
    // Manages user authentication tokens and session data.
    private lateinit var tokenManger: TokenManger

    // Inflates the layout and initializes the binding for the fragment.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainSettingsBinding.inflate(layoutInflater, container, false )
        return binding.root
    }

    // Sets up UI listeners and fetches user settings after the view is created.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tokenManger = TokenManger(requireContext())
        setButtonListeners()
        getUserSettings()
    }

    // Cleans up the binding to prevent memory leaks when the fragment is destroyed.
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // Prepares the language dropdown spinner with available options and sets the current selection.
    private fun prepareLanguageSpinner(selectedCode: String? = null) {
        val languages = resources.getStringArray(R.array.language_options)
        val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown_item, languages)
        binding.languageDropdown.setAdapter(adapter)
        val displayName = selectedCode?.let { languageMap[it] } ?: languages[0]
        binding.languageDropdown.setText(displayName, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    // Attaches click and touch listeners for all interactive UI elements, including help, about, privacy, and settings switches.
    private fun setButtonListeners(){
        binding.helpAndSupportTab.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
            false
        }
        binding.helpAndSupportTab.setOnClickListener {
            val intent = Intent(requireContext(), FAQChatBot::class.java)
            startActivity(intent)
        }
        binding.aboutTab.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
            false
        }
        binding.privacyPolicyTab.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
            false
        }
        binding.privacyPolicyTab.setOnClickListener {
            commitFragmentToContainer(PrivacyPolicyFragment())
        }
        binding.notificationSwitch.setOnCheckedChangeListener { _, _ ->
            if (!isInitializing) updateUserSettings()
        }
        binding.offlineSyncSwitch.setOnCheckedChangeListener { _, _ ->
            if (!isInitializing) updateUserSettings()
        }
        binding.languageDropdown.setOnItemClickListener { _, _, _, _ ->
            updateUserSettings()
        }
    }

    // Replaces the current fragment with the provided fragment in the container.
    private fun commitFragmentToContainer(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    // Collects user settings from the UI and sends an update request to the backend.
    private fun updateUserSettings(){
        val userId = tokenManger.getUser() ?: return
        val notificationSelection = binding.notificationSwitch.isChecked.toString()
        val offlineSyncSelection = binding.offlineSyncSwitch.isChecked.toString()
        val selectedDisplayName = binding.languageDropdown.text.toString()
        val selectedLanguageCode = languageMap.entries.find { it.value == selectedDisplayName }?.key ?: "en"
        parts.clear()
        createPart("preferredLanguage", selectedLanguageCode)?.let { parts.add(it) }
        createPart("notifications", notificationSelection)?.let { parts.add(it) }
        createPart("offlineSync", offlineSyncSelection)?.let { parts.add(it) }
        val api = RetrofitInstance.createAPIInstance(requireContext())
        api.updateUserSettings(userId, parts).enqueue( object : Callback<UpdateSettingsResponse> {
            override fun onResponse(
                call: Call<UpdateSettingsResponse?>,
                response: Response<UpdateSettingsResponse?>
            ) {
                if (response.isSuccessful){
                    val userSettings = response.body()
                    if (userSettings != null) {
                        CustomToast.show(requireContext(), "Settings Updated", CustomToast.Companion.ToastType.SUCCESS)
                    }
                }
                else{
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = if (errorBody != null) {
                        try {
                            val json = JSONObject(errorBody)
                            json.getString("error")
                        } catch (e: Exception) {
                            "Unknown error"
                        }
                    } else {
                        "Unknown error"
                    }
                    if (response.code() == 401) {
                        tokenManger.clearToken()
                        tokenManger.clearUser()
                        tokenManger.clearPfp()
                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    CustomToast.show(requireContext(), errorMessage, CustomToast.Companion.ToastType.ERROR)
                }
            }

            override fun onFailure(
                call: Call<UpdateSettingsResponse?>,
                t: Throwable
            ) {
                CustomToast.show(requireContext(), "${t.message}", CustomToast.Companion.ToastType.ERROR)
                Log.e("Profile Settings", "Error: ${t.message.toString()}")
            }
        })
    }

    // Fetches user settings from the backend and binds them to the UI, handling authentication errors.
    private fun getUserSettings(){
        showSettingsOverlay()
        val userId = tokenManger.getUser()
        if (userId != null) {
            val api = RetrofitInstance.createAPIInstance(requireContext())
            api.getUserById(userId).enqueue(object : Callback<UserSettingsResponse> {
                override fun onResponse(
                    call: Call<UserSettingsResponse>,
                    response: Response<UserSettingsResponse>
                ) {
                    if (response.isSuccessful){
                        hideSettingsOverlay()
                        val userSettings = response.body()
                        if (userSettings != null) {
                            binding.notificationSwitch.isChecked = userSettings.profile?.notifications ?: false
                            binding.offlineSyncSwitch.isChecked = userSettings.profile?.offlineSync ?: false
                            prepareLanguageSpinner(userSettings.profile?.preferredLanguage)
                            isInitializing = false
                        }
                    }
                    else{
                        hideSettingsOverlay()
                        val errorBody = response.errorBody()?.string()
                        val errorMessage = if (errorBody != null) {
                            try {
                                val json = JSONObject(errorBody)
                                json.getString("error")
                            } catch (e: Exception) {
                                "Unknown error"
                            }
                        } else {
                            "Unknown error"
                        }
                        if (response.code() == 401) {
                            tokenManger.clearToken()
                            tokenManger.clearUser()
                            tokenManger.clearPfp()
                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        CustomToast.show(requireContext(), errorMessage, CustomToast.Companion.ToastType.ERROR)
                    }
                }

                override fun onFailure(call: Call<UserSettingsResponse>, t: Throwable) {
                    hideSettingsOverlay()
                    CustomToast.show(requireContext(), "${t.message}", CustomToast.Companion.ToastType.ERROR)
                    Log.e("Profile Settings", "Error: ${t.message.toString()}")
                }
            })
        }
    }

    // Creates a multipart form data part for a given key-value pair, only if the value is not blank.
    private fun createPart(key: String, value: String?): MultipartBody.Part? {
        return value?.takeIf { it.isNotBlank() }?.let {
            val requestBody = it.toRequestBody("text/plain".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(key, null, requestBody)
        }
    }

    // Shows a loading overlay while user settings are being fetched or updated.
    private fun showSettingsOverlay(){
        binding.settingsOverlay.visibility = View.VISIBLE
    }
    // Hides the loading overlay after user settings are loaded or updated.
    private fun hideSettingsOverlay(){
        binding.settingsOverlay.visibility = View.GONE
    }
}
