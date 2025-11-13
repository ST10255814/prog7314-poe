package com.example.rentwise.maintenance

import com.example.rentwise.retrofit_instance.RetrofitInstance
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rentwise.R
import com.example.rentwise.adapters.FileAttachmentAdapter
import com.example.rentwise.auth.LoginActivity
import com.example.rentwise.custom_toast.CustomToast
import com.example.rentwise.data_classes.BookedListings
import com.example.rentwise.data_classes.ListingDropDownItem
import com.example.rentwise.data_classes.ListingResponse
import com.example.rentwise.databinding.FragmentMaintenanceBinding
import com.example.rentwise.shared_pref_config.TokenManger
import com.example.rentwise.utils.LocaleHelper
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Callback

// Fragment for submitting maintenance requests, handling file attachments, dropdowns, and API calls.
class MaintenanceFragment : Fragment() {
    // Binds the layout for the maintenance request form and manages UI elements.
    private var _binding: FragmentMaintenanceBinding? = null
    private val binding get() = _binding!!
    // Handles user authentication and session data.
    private lateinit var tokenManager: TokenManger
    // Stores available units for the dropdown, populated from API.
    private var unitDropdown = mutableListOf<ListingDropDownItem>()
    // Adapter for displaying attached files in a RecyclerView.
    private lateinit var fileAdapter: FileAttachmentAdapter
    // Tracks URIs of files attached by the user.
    private val filesAttached = mutableListOf<Uri>()

    // OVERRIDE ONATTACH TO APPLY SAVED LOCALE
    // This ensures the saved language is applied when the fragment is attached
    override fun onAttach(context: Context) {
        super.onAttach(LocaleHelper.onAttach(context))
    }

    // Inflates the fragment layout and initializes view binding.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaintenanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Cleans up the binding to prevent memory leaks when the fragment is destroyed.
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // Sets up listeners, dropdowns, and file attachment RecyclerView after the view is created.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManger(requireContext())

        setListeners() // Attaches click and touch listeners for UI controls.
        updateDropdowns() // Populates priority and unit dropdowns from resources and API.
        setupRecyclerView() // Configures RecyclerView for displaying attached files.
    }

    // Populates the priority dropdown from resources and fetches available units for the unit dropdown.
    private fun updateDropdowns() {
        if (!isAdded || _binding == null) return
        val priority = resources.getStringArray(R.array.priority_levels).toList()
        val priorityAdapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown_item, priority)

        binding.priorityDropdown.setAdapter(priorityAdapter)
        binding.priorityDropdown.setText(priority[0], false) // Set initial value to first value

        getAllListingsForDropDown() // Fetches available units from the backend.
    }

    @SuppressLint("ClickableViewAccessibility")
    // Sets up click and touch listeners for file upload and request submission buttons.
    private fun setListeners(){
        binding.btnUploadFile.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            false
        }
        binding.btnSubmitRequest.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            false
        }
        binding.btnUploadFile.setOnClickListener {
            // Launches the file picker for image selection.
            filePickerLauncher.launch("*/*")
        }
        binding.btnSubmitRequest.setOnClickListener {
            createMaintenanceTicket() // Initiates maintenance request submission.
        }
    }

    // Validates input, prepares multipart request, and submits a maintenance ticket via API.
    private fun createMaintenanceTicket(){
        showSubmitLoadingOverlay()
        val api = RetrofitInstance.createAPIInstance(requireContext())
        val userID = tokenManager.getUser() ?: return

        val issue = binding.editIssueTitle.text.toString().trim()
        val description = binding.editIssueDescription.text.toString().trim()
        val listing = binding.unitDropdown.text.toString().trim()
        val priority = binding.priorityDropdown.text.toString().trim()

        // Ensures all required fields are filled and description is sufficiently detailed.
        if (issue.isEmpty() || description.isEmpty() || listing.isEmpty() || priority.isEmpty()) {
            CustomToast.show(requireContext(), "Please fill in all fields", CustomToast.Companion.ToastType.ERROR)
            hideSubmitLoadingOverlay()
            return
        }

        if(description.length < 25){
            CustomToast.show(requireContext(), "Description should be at least 25 characters long", CustomToast.Companion.ToastType.ERROR)
            hideSubmitLoadingOverlay()
            return
        }

        // Finds the selected unit's ID from the dropdown.
        val listingID = unitDropdown.find { it.name == listing }?.id ?: run {
            CustomToast.show(
                requireContext(),
                "Please select a valid unit",
                CustomToast.Companion.ToastType.ERROR
            )
            hideSubmitLoadingOverlay()
            return
        }

        // Prepares text fields as RequestBody parts for the API call.
        val issuePart = issue.toRequestBody("text/plain".toMediaTypeOrNull())
        val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val priorityPart = priority.toRequestBody("text/plain".toMediaTypeOrNull())

        // Converts attached files into MultipartBody.Part objects for upload.
        val documentParts = mutableListOf<MultipartBody.Part>()
        for(uri in filesAttached) {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val fileBytes = inputStream!!.readBytes()
            val mimeType = requireContext().contentResolver.getType(uri) ?: "application/octet-stream"
            val body = MultipartBody.Part.createFormData(
                "documentURL",
                getFileName(uri),
                RequestBody.create(mimeType.toMediaTypeOrNull(), fileBytes)
            )
            documentParts.add(body)
        }

        // Requires at least one file to be attached before submission.
        if(documentParts.isEmpty()){
            CustomToast.show(requireContext(), "Please attach at least one file", CustomToast.Companion.ToastType.ERROR)
            hideSubmitLoadingOverlay()
            return
        }

        // Submits the maintenance request to the backend and handles the response.
        api.createMaintenanceRequest(userID, listingID, issuePart, descriptionPart, priorityPart, documentParts)
            .enqueue(object : Callback<com.example.rentwise.data_classes.MaintenanceResponse> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(
                    call: retrofit2.Call<com.example.rentwise.data_classes.MaintenanceResponse>,
                    response: retrofit2.Response<com.example.rentwise.data_classes.MaintenanceResponse>
                ) {
                    if (!isAdded || _binding == null) return
                    if (response.isSuccessful) {
                        hideSubmitLoadingOverlay()
                        val responseBody = response.body()
                        val message = responseBody?.message ?: "Maintenance request created successfully"
                        CustomToast.show(requireContext(), message, CustomToast.Companion.ToastType.SUCCESS)

                        // Resets form fields and clears attached files after successful submission.
                        binding.editIssueTitle.text?.clear()
                        binding.editIssueDescription.text?.clear()
                        if (unitDropdown.isNotEmpty()) {
                            binding.unitDropdown.setText(unitDropdown[0].name, false)
                        }
                        val priorityLevels = resources.getStringArray(R.array.priority_levels).toList()
                        binding.priorityDropdown.setText(priorityLevels[0], false)
                        filesAttached.clear()
                        fileAdapter.notifyDataSetChanged()

                        val intent = Intent(requireContext(), MaintenanceRequest::class.java)
                        startActivity(intent)

                    } else {
                        // Handles error responses, including unauthorized access.
                        hideSubmitLoadingOverlay()
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
                            tokenManager.clearToken()
                            tokenManager.clearUser()
                            tokenManager.clearPfp()

                            CustomToast.show(requireContext(), getString(R.string.session_expired_message),
                                CustomToast.Companion.ToastType.ERROR)
                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        CustomToast.show(requireContext(), errorMessage, CustomToast.Companion.ToastType.ERROR)
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<com.example.rentwise.data_classes.MaintenanceResponse>,
                    t: Throwable
                ) {
                    // Handles network or unexpected errors during submission.
                    if (!isAdded || _binding == null) return
                    hideSubmitLoadingOverlay()
                    CustomToast.show(requireContext(), t.message.toString(), CustomToast.Companion.ToastType.ERROR)
                    Log.e("Error", t.message.toString())
                }
            })
    }

    // Fetches all available listings for the unit dropdown, filtering out invalid entries.
    private fun getAllListingsForDropDown() {
        showLoadingOverlay()
        val userId = tokenManager.getUser() ?: return
        val api = RetrofitInstance.createAPIInstance(requireContext())
        api.getBookedListings(userId).enqueue(object : Callback<List<BookedListings>> {
            override fun onResponse(
                call: retrofit2.Call<List<BookedListings>>,
                response: retrofit2.Response<List<BookedListings>>
            ) {
                if (!isAdded || _binding == null) return
                hideLoadingOverlay()

                if (response.isSuccessful) {
                    val listings = response.body()
                    if (!listings.isNullOrEmpty()) {
                        // Build dropdown items and adapter
                        unitDropdown = listings.mapNotNull { listing ->
                            val title = listing.title
                            val id = listing.listingId
                            if (!title.isNullOrEmpty() && !id.isNullOrEmpty()) {
                                ListingDropDownItem(id, title)
                            } else null
                        }.toMutableList()

                        // Prepare a list of display names for the AutoComplete adapter
                        val names = unitDropdown.map { it.name }

                        val adapter = ArrayAdapter(
                            requireContext(),
                            R.layout.custom_spinner_dropdown_item,
                            names
                        )

                        binding.unitDropdown.setAdapter(adapter)
                        binding.unitDropdown.isEnabled = true
                        binding.unitDropdown.isClickable = true
                        binding.unitDropdown.isFocusable = true

                        if (unitDropdown.isNotEmpty()) {
                            binding.unitDropdown.setText(unitDropdown[0].name, false)
                        }
                    } else {
                        // No bookings available: show message and disable dropdown
                        val msg = getString(R.string.no_bookings_to_select_from)
                        val adapter = ArrayAdapter(
                            requireContext(),
                            R.layout.custom_spinner_dropdown_item,
                            listOf(msg)
                        )
                        binding.unitDropdown.setAdapter(adapter)
                        binding.unitDropdown.setText(msg, false)
                        binding.unitDropdown.isEnabled = false
                        binding.unitDropdown.isClickable = false
                        binding.unitDropdown.isFocusable = false
                    }
                } else {
                    // Handles error responses and logs out if unauthorized.
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
                        tokenManager.clearToken()
                        tokenManager.clearUser()
                        tokenManager.clearPfp()

                        CustomToast.show(requireContext(), getString(R.string.session_expired_message),
                            CustomToast.Companion.ToastType.ERROR)
                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                    CustomToast.show(requireContext(), errorMessage, CustomToast.Companion.ToastType.ERROR)
                }
            }

            override fun onFailure(call: retrofit2.Call<List<BookedListings>>, t: Throwable) {
                if (!isAdded || _binding == null) return
                hideLoadingOverlay()
                CustomToast.show(requireContext(), t.message.toString(), CustomToast.Companion.ToastType.ERROR)
                Log.e("Maintenance", t.message.toString())
            }
        })
    }
    // Displays a loading overlay while listings are being fetched.
    private fun showLoadingOverlay(){
        binding.overlayLoadingProperties.visibility = View.VISIBLE
    }

    // Hides the loading overlay after listings are loaded.
    private fun hideLoadingOverlay(){
        binding.overlayLoadingProperties.visibility = View.GONE
    }

    // Shows a loading overlay during maintenance request submission.
    private fun showSubmitLoadingOverlay(){
        binding.submissionOverlay.visibility = View.VISIBLE
    }
    // Hides the submission loading overlay after request is processed.
    private fun hideSubmitLoadingOverlay(){
        binding.submissionOverlay.visibility = View.GONE
    }

    // Specifies allowed MIME types for file attachments.
    private val allowedMimeTypes = arrayOf(
        "image/jpeg",
        "image/png"
    )

    // Handles file selection, validates type, and updates the attached files list.
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val mimeType = requireContext().contentResolver.getType(uri)
            if (mimeType !in allowedMimeTypes) {
                CustomToast.show(requireContext(), "Invalid file type", CustomToast.Companion.ToastType.ERROR)
                return@registerForActivityResult
            }
            if (filesAttached.contains(uri)) {
                CustomToast.show(requireContext(), "File already attached", CustomToast.Companion.ToastType.ERROR)
            } else {
                filesAttached.add(uri)
                fileAdapter.notifyItemInserted(filesAttached.size - 1)
                CustomToast.show(requireContext(), "Selected file: ${getFileName(uri)}", CustomToast.Companion.ToastType.INFO)
            }
        }
    }

    // Retrieves the display name of a file from its URI for user feedback and upload.
    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path?.substringAfterLast('/') ?: "unknown_file"
        }
        return result ?: "unknown_file"
    }

    // Configures the RecyclerView to display attached files and handle file removal.
    private fun setupRecyclerView() {
        fileAdapter = FileAttachmentAdapter(filesAttached) { position ->
            filesAttached.removeAt(position)
            fileAdapter.notifyItemRemoved(position)
        }
        binding.rvUploadedFiles.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUploadedFiles.adapter = fileAdapter
    }
}

