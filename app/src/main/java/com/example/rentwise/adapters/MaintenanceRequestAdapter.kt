package com.example.rentwise.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rentwise.R
import com.example.rentwise.data_classes.MaintenanceRequestData
import com.example.rentwise.databinding.ItemMaintenanceRequestBinding

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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val r = requests[position]

        holder.binding.apply {
            tvRequestTitle.text = r.
            tvRequestDescription.text = r.description
            tvRequestUnit.text = "Unit: ${r.unit}"
            tvAssignedStaff.text = "Assigned Caretaker: ${r.assignedStaff}"
            tvRequestDate.text = "Submitted On: ${r.dateSubmitted}"
            tvRequestId.text = "Maintenance Request ID: ${r.id}"
            tvFollowUpRequests.text = "Follow-ups: ${r.followUps}"
            tvCaretakerNote.text = "Caretaker Note: ${r.caretakerNote}"

            // Set Priority Badge color
            tvRequestPriority.text = r.priority
            val priorityBg = when(r.priority.lowercase()){
                "high" -> R.drawable.bg_priority_high
                "medium" -> R.drawable.bg_priority_medium
                else -> R.drawable.bg_priority_low
            }
            tvRequestPriority.setBackgroundResource(priorityBg)

            // Set Status Badge color
            tvRequestStatus.text = r.status
            val statusBg = when(r.status.lowercase()){
                "pending" -> R.drawable.bg_status_pending
                "in progress" -> R.drawable.bg_status_in_progress
                "completed" -> R.drawable.bg_status_completed
                else -> R.drawable.bg_status_pending
            }
            tvRequestStatus.setBackgroundResource(statusBg)
        }
    }
}
