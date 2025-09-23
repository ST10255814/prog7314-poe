package com.example.rentwise.maintenance

import RetrofitInstance
import android.annotation.SuppressLint
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
import com.example.rentwise.data_classes.ListingDropDownItem
import com.example.rentwise.data_classes.ListingResponse
import com.example.rentwise.databinding.FragmentMaintenanceBinding
import com.example.rentwise.shared_pref_config.TokenManger
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Callback

class MaintenanceFragment : Fragment() {
    private var _binding: FragmentMaintenanceBinding? = null
    private val binding get() = _binding!!
    private lateinit var tokenManager: TokenManger
    private var unitDropdown = mutableListOf<ListingDropDownItem>()
    private lateinit var fileAdapter: FileAttachmentAdapter
    private val filesAttached = mutableListOf<Uri>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaintenanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tokenManager = TokenManger(requireContext())

        setListeners()
        updateDropdowns()
        setupRecyclerView()
    }

    private fun updateDropdowns() {
        if (!isAdded || _binding == null) return
        val priority = resources.getStringArray(R.array.priority_levels).toList()
        val priorityAdapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_dropdown_item, priority)

        binding.priorityDropdown.setAdapter(priorityAdapter)
        binding.priorityDropdown.setText(priority[0], false)

        getAllListingsForDropDown()
    }

    @SuppressLint("ClickableViewAccessibility")
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
            // Launch file picker for images
            filePickerLauncher.launch("*/*")
        }
        binding.btnSubmitRequest.setOnClickListener {
            createMaintenanceTicket()
        }
    }

    private fun createMaintenanceTicket(){
        showSubmitLoadingOverlay()
        val api = RetrofitInstance.createAPIInstance(requireContext())
        val userID = tokenManager.getUser() ?: return

        // Validate inputs
        val issue = binding.editIssueTitle.text.toString().trim()
        val description = binding.editIssueDescription.text.toString().trim()
        val listing = binding.unitDropdown.text.toString().trim()
        val priority = binding.priorityDropdown.text.toString().trim()

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

        //Find listing ID from dropdown selection
        val listingID = unitDropdown.find { it.name == listing }?.id ?: run {
            CustomToast.show(
                requireContext(),
                "Please select a valid unit",
                CustomToast.Companion.ToastType.ERROR
            )
            hideSubmitLoadingOverlay()
            return
        }

        // Prepare RequestBody parts
        val issuePart = issue.toRequestBody("text/plain".toMediaTypeOrNull())
        val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val priorityPart = priority.toRequestBody("text/plain".toMediaTypeOrNull())

        //Prepare file parts
        val documentParts = mutableListOf<MultipartBody.Part>()
        for(uri in filesAttached) {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val fileBytes = inputStream!!.readBytes() // Read file bytes
            val mimeType = requireContext().contentResolver.getType(uri) ?: "application/octet-stream"
            val body = MultipartBody.Part.createFormData(
                "documentURL",
                getFileName(uri),
                RequestBody.create(mimeType.toMediaTypeOrNull(), fileBytes)
            )
            documentParts.add(body)
        }

        // Ensure at least one file is attached
        if(documentParts.isEmpty()){
            CustomToast.show(requireContext(), "Please attach at least one file", CustomToast.Companion.ToastType.ERROR)
            hideSubmitLoadingOverlay()
            return
        }

        // Make API call with prepared parts
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

                        // Clear inputs and reset dropdowns
                        binding.editIssueTitle.text?.clear()
                        binding.editIssueDescription.text?.clear()
                        if (unitDropdown.isNotEmpty()) {
                            binding.unitDropdown.setText(unitDropdown[0].name, false)
                        }
                        val priorityLevels = resources.getStringArray(R.array.priority_levels).toList()
                        binding.priorityDropdown.setText(priorityLevels[0], false)
                        filesAttached.clear()
                        fileAdapter.notifyDataSetChanged()

                    } else {
                        // Handle error response
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
                        // Log out if unauthorized
                        if (response.code() == 401) {
                            tokenManager.clearToken()
                            tokenManager.clearUser()
                            tokenManager.clearPfp()

                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //Clear Activity trace
                            startActivity(intent)
                        }
                        CustomToast.show(requireContext(), errorMessage, CustomToast.Companion.ToastType.ERROR)
                    }
                }

                override fun onFailure(
                    call: retrofit2.Call<com.example.rentwise.data_classes.MaintenanceResponse>,
                    t: Throwable
                ) {
                    // Handle failure
                    if (!isAdded || _binding == null) return
                    hideSubmitLoadingOverlay()
                    CustomToast.show(requireContext(), t.message.toString(), CustomToast.Companion.ToastType.ERROR)
                    Log.e("Error", t.message.toString())
                }
            })
    }

    private fun getAllListingsForDropDown() {
        showLoadingOverlay()
        val api = RetrofitInstance.createAPIInstance(requireContext())
        api.getListings().enqueue(object : Callback<List<ListingResponse>> {
            override fun onResponse(
                call: retrofit2.Call<List<ListingResponse>>,
                response: retrofit2.Response<List<ListingResponse>>
            ) {
                if (!isAdded || _binding == null) return
                if (response.isSuccessful) {
                    hideLoadingOverlay()
                    val listings = response.body()
                    //  Check for null or empty list
                    if (!listings.isNullOrEmpty()) {
                        // Map and filter out nulls
                        unitDropdown = listings.mapNotNull { listing ->
                            val title = listing.title
                            val id = listing.propertyId

                            // Only include if both title and id are non-null and non-empty
                            if (!title.isNullOrEmpty() && !id.isNullOrEmpty()) {
                                ListingDropDownItem(id, title) // Add to the list
                            } else null
                        }.toMutableList()

                        val adapter = ArrayAdapter(
                            requireContext(),
                            R.layout.custom_spinner_dropdown_item,
                            unitDropdown
                        )

                        binding.unitDropdown.setAdapter(adapter)

                        if (unitDropdown.isNotEmpty()) {
                            binding.unitDropdown.setText(unitDropdown[0].name, false)
                        }
                    }
                }
                else{
                    // Handle error response
                    hideLoadingOverlay()
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
                    // Log out if unauthorized
                    if (response.code() == 401) {
                        tokenManager.clearToken()
                        tokenManager.clearUser()
                        tokenManager.clearPfp()

                        val intent = Intent(requireContext(), LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK //Clear Activity trace
                        startActivity(intent)
                    }
                    CustomToast.show(requireContext(), errorMessage, CustomToast.Companion.ToastType.ERROR)
                }
            }
            override fun onFailure(call: retrofit2.Call<List<ListingResponse>>, t: Throwable) {
                // Handle failure
                if (!isAdded || _binding == null) return
                hideLoadingOverlay()
                CustomToast.show(requireContext(), t.message.toString(), CustomToast.Companion.ToastType.ERROR)
                Log.e("Error", t.message.toString())
            }
        })
    }

    private fun showLoadingOverlay(){
        binding.overlayLoadingProperties.visibility = View.VISIBLE
    }

    private fun hideLoadingOverlay(){
        binding.overlayLoadingProperties.visibility = View.GONE
    }

    private fun showSubmitLoadingOverlay(){
        binding.submissionOverlay.visibility = View.VISIBLE
    }
    private fun hideSubmitLoadingOverlay(){
        binding.submissionOverlay.visibility = View.GONE
    }

    // Allowed MIME types for file selection
    private val allowedMimeTypes = arrayOf(
        "image/jpeg", // jpg, jpeg
        "image/png"  // png
    )

    // File picker launcher
    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        // Check if a file was selected
        if (uri != null) {
            val mimeType = requireContext().contentResolver.getType(uri)
            // Validate MIME type
            if (mimeType !in allowedMimeTypes) {
                CustomToast.show(requireContext(), "Invalid file type", CustomToast.Companion.ToastType.ERROR)
                return@registerForActivityResult
            }
            // Check for duplicates
            if (filesAttached.contains(uri)) {
                CustomToast.show(requireContext(), "File already attached", CustomToast.Companion.ToastType.ERROR)
            } else {
                // Add new file
                filesAttached.add(uri)
                fileAdapter.notifyItemInserted(filesAttached.size - 1)
                CustomToast.show(requireContext(), "Selected file: ${getFileName(uri)}", CustomToast.Companion.ToastType.INFO)
            }
        }
    }

    // Get file name from URI
    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") { // Use content resolver to get file name
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

    // Setup RecyclerView for displaying selected docs
    private fun setupRecyclerView() {
        fileAdapter = FileAttachmentAdapter(filesAttached) { position ->
            filesAttached.removeAt(position)
            fileAdapter.notifyItemRemoved(position)
        }
        binding.rvUploadedFiles.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUploadedFiles.adapter = fileAdapter
    }
}