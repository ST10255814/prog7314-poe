package com.example.rentwise.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rentwise.R
import com.example.rentwise.data_classes.MaintenanceRequestResponse
import com.example.rentwise.databinding.ItemMaintenanceRequestBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

// Adapter for displaying a list of maintenance requests in a RecyclerView.
// Each item shows details such as issue, description, unit, assigned caretaker, date, ID, follow-ups, notes, priority, and status.
class MaintenanceRequestAdapter(
    private val requests: List<MaintenanceRequestResponse>
) : RecyclerView.Adapter<MaintenanceRequestAdapter.ViewHolder>() {

    // ViewHolder class that holds the binding for each maintenance request item layout.
    inner class ViewHolder(val binding: ItemMaintenanceRequestBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Inflates the item layout and creates a new ViewHolder for each maintenance request.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMaintenanceRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    // Returns the total number of maintenance requests to be displayed.
    override fun getItemCount(): Int = requests.size

    @SuppressLint("SetTextI18n")
    // Binds the maintenance request data to the ViewHolder, populating all relevant fields and setting badge backgrounds.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val issue = requests[position]

        holder.binding.apply {
            // Sets the issue title or an empty string if unavailable.
            tvRequestTitle.text = issue.newMaintenanceRequest?.issue ?: ""
            // Sets the issue description or an empty string if unavailable.
            tvRequestDescription.text = issue.newMaintenanceRequest?.description ?: ""
            // Displays the unit title associated with the request.
            tvRequestUnit.text = "Unit: ${issue.listingDetail?.title}"
            // Shows the assigned caretaker's name or "Unassigned" if not assigned.
            tvAssignedStaff.text = "Assigned Caretaker: ${issue.newMaintenanceRequest?.caretakerId ?: "Unassigned"}"
            // Formats and displays the submission date of the request.
            tvRequestDate.text = "Submitted On: ${formatDate(issue.newMaintenanceRequest?.createdAt)}"
            // Shows the unique maintenance request ID.
            tvRequestId.text = "Maintenance Request ID: ${issue.newMaintenanceRequest?.maintenanceId}"
            // Displays the number of follow-up requests or "0" if none.
            tvFollowUpRequests.text = "Follow-ups: ${issue.followUps ?: "0"}"
            // Shows caretaker notes or "No Notes" if none are provided.
            tvCaretakerNote.text = "Caretaker Note: ${issue.newMaintenanceRequest?.careTakerNotes ?: "No Notes"}"

            // Sets the priority text and applies a background color based on the priority level.
            tvRequestPriority.text = issue.newMaintenanceRequest?.priority ?: ""
            val priorityBg = when(issue.newMaintenanceRequest?.priority?.lowercase()){
                "high" -> R.drawable.bg_priority_high
                "medium" -> R.drawable.bg_priority_medium
                else -> R.drawable.bg_priority_low
            }
            tvRequestPriority.setBackgroundResource(priorityBg)

            // Sets the status text and applies a background color based on the current status.
            tvRequestStatus.text = issue.newMaintenanceRequest?.status ?: "Pending"
            val statusBg = when(issue.newMaintenanceRequest?.status?.lowercase()){
                "pending" -> R.drawable.bg_status_pending
                "in progress" -> R.drawable.bg_status_in_progress
                "completed" -> R.drawable.bg_status_completed
                else -> R.drawable.bg_status_pending
            }
            tvRequestStatus.setBackgroundResource(statusBg)
        }
    }

    // Converts the MongoDB date string to a human-readable format for display.
    // If parsing fails, returns the original string as a fallback.
    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)

            val outputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.getDefault())
            outputFormat.format(date!!)
        } catch (e: Exception) {
            dateString
        }
    }
}
