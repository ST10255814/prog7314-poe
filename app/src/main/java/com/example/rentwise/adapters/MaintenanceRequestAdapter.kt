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

class MaintenanceRequestAdapter(
    private val requests: List<MaintenanceRequestResponse>
) : RecyclerView.Adapter<MaintenanceRequestAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemMaintenanceRequestBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMaintenanceRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = requests.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val issue = requests[position]

        //Apply the content to the different fields
        holder.binding.apply {
            tvRequestTitle.text = issue.newMaintenanceRequest?.issue ?: ""
            tvRequestDescription.text = issue.newMaintenanceRequest?.description ?: ""
            tvRequestUnit.text = "Unit: ${issue.listingDetail?.title}"
            tvAssignedStaff.text = "Assigned Caretaker: ${issue.assignedCaretaker ?: "Unassigned"}"
            tvRequestDate.text = "Submitted On: ${formatDate(issue.newMaintenanceRequest?.createdAt)}"
            tvRequestId.text = "Maintenance Request ID: ${issue._id}"
            tvFollowUpRequests.text = "Follow-ups: ${issue.followUps ?: "0"}"
            tvCaretakerNote.text = "Caretaker Note: ${issue.careTakerNotes ?: "No Notes"}"

            // Set Priority Badge color depending on priority text
            tvRequestPriority.text = issue.newMaintenanceRequest?.priority ?: ""
            val priorityBg = when(issue.newMaintenanceRequest?.priority?.lowercase()){
                "high" -> R.drawable.bg_priority_high
                "medium" -> R.drawable.bg_priority_medium
                else -> R.drawable.bg_priority_low
            }
            tvRequestPriority.setBackgroundResource(priorityBg)

            // Set Status Badge color depending on status text
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

    //Function to display the date in the correct format opposed to the default mongo time
    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC") // UTC time zone used to represent the 'Z'
            val date = inputFormat.parse(dateString)

            val outputFormat = SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.getDefault())
            outputFormat.format(date!!) //Output null assert date
        } catch (e: Exception) {
            dateString // fallback to original if parsing fails
        }
    }
}
