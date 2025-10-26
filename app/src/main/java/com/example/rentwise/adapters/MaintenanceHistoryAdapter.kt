package com.example.rentwise.maintenance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.rentwise.R
import com.example.rentwise.data_classes.MaintenanceRequestResponse
import java.text.SimpleDateFormat
import java.util.Locale

// Binds MaintenanceRequestResponse to item_maintenance_request.xml cards (no heavy logic).
class MaintenanceHistoryAdapter(
    private val items: List<MaintenanceRequestResponse>
) : RecyclerView.Adapter<MaintenanceHistoryAdapter.MVH>() {

    class MVH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvRequestTitle)
        val tvPriority: TextView = view.findViewById(R.id.tvRequestPriority)
        val tvStatus: TextView = view.findViewById(R.id.tvRequestStatus)
        val tvDesc: TextView = view.findViewById(R.id.tvRequestDescription)
        val tvUnit: TextView = view.findViewById(R.id.tvRequestUnit)
        val tvStaff: TextView = view.findViewById(R.id.tvAssignedStaff)
        val tvDate: TextView = view.findViewById(R.id.tvRequestDate)
        val tvId: TextView = view.findViewById(R.id.tvRequestId)
        val tvFollowUps: TextView = view.findViewById(R.id.tvFollowUpRequests)
        val tvCaretakerNote: TextView = view.findViewById(R.id.tvCaretakerNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_maintenance_request, parent, false)
        return MVH(v)
    }

    override fun onBindViewHolder(h: MVH, pos: Int) {
        val item = items[pos]
        val detail = item.newMaintenanceRequest

        h.tvTitle.text = detail?.issue ?: "Maintenance Request"
        h.tvDesc.text = detail?.description ?: "—"
        h.tvPriority.text = priorityLabel(detail?.priority)
        h.tvStatus.text = statusLabel(detail?.status)
        h.tvUnit.text = "Unit: ${item.listingDetail?.title ?: "—"}"
        h.tvStaff.text = "Assigned: ${item.assignedCaretaker ?: "—"}"
        h.tvDate.text = "Submitted: ${formatDate(detail?.createdAt)}"
        h.tvId.text = "Request ID: #${item._id ?: "—"}"
        h.tvFollowUps.text = "Follow-ups: ${item.followUps ?: 0}"
        h.tvCaretakerNote.text = "Caretaker Note: ${item.careTakerNotes ?: "—"}"

        // ---- Runtime visuals (keeps your layout unchanged) ----
        // Priority background + text color override
        val ctx = h.itemView.context
        val prio = (detail?.priority ?: "").lowercase(Locale.US)
        when (prio) {
            "low" -> {
                h.tvPriority.background = ContextCompat.getDrawable(ctx, R.drawable.bg_priority_low)
                h.tvPriority.setTextColor(ContextCompat.getColor(ctx, android.R.color.white))
            }
            "medium" -> {
                h.tvPriority.background = ContextCompat.getDrawable(ctx, R.drawable.bg_priority_medium)
                h.tvPriority.setTextColor(ContextCompat.getColor(ctx, android.R.color.white))
            }
            "high" -> {
                // You already had bg_priority_high on the layout; we re-apply here to be explicit.
                h.tvPriority.background = ContextCompat.getDrawable(ctx, R.drawable.bg_priority_high)
                h.tvPriority.setTextColor(ContextCompat.getColor(ctx, android.R.color.white))
            }
            else -> {
                // Fallback to "medium" styling if API sends something unexpected
                h.tvPriority.background = ContextCompat.getDrawable(ctx, R.drawable.bg_priority_medium)
                h.tvPriority.setTextColor(ContextCompat.getColor(ctx, android.R.color.white))
            }
        }

        // Status background + text color override
        val status = (detail?.status ?: "").lowercase(Locale.US)
        when (status) {
            "pending" -> {
                h.tvStatus.background = ContextCompat.getDrawable(ctx, R.drawable.bg_status_pending)
                h.tvStatus.setTextColor(ContextCompat.getColor(ctx, android.R.color.white))
            }
            "in-progress", "in_progress", "in progress" -> {
                h.tvStatus.background = ContextCompat.getDrawable(ctx, R.drawable.bg_status_in_progress)
                h.tvStatus.setTextColor(ContextCompat.getColor(ctx, android.R.color.white))
            }
            "resolved", "closed", "completed" -> {
                h.tvStatus.background = ContextCompat.getDrawable(ctx, R.drawable.bg_status_resolved)
                h.tvStatus.setTextColor(ContextCompat.getColor(ctx, android.R.color.white))
            }
            else -> {
                // default to pending style
                h.tvStatus.background = ContextCompat.getDrawable(ctx, R.drawable.bg_status_pending)
                h.tvStatus.setTextColor(ContextCompat.getColor(ctx, android.R.color.white))
            }
        }
    }

    override fun getItemCount(): Int = items.size

    private fun priorityLabel(p: String?): String = when ((p ?: "").lowercase(Locale.US)) {
        "low" -> "Low Priority"
        "medium" -> "Medium Priority"
        "high" -> "High Priority"
        else -> (p ?: "Priority")
    }

    private fun statusLabel(s: String?): String = when ((s ?: "").lowercase(Locale.US)) {
        "pending" -> "Pending"
        "in-progress", "in_progress", "in progress" -> "In-Progress"
        "resolved", "closed", "completed" -> "Resolved"
        else -> (s ?: "Status")
    }

    private fun formatDate(iso: String?): String {
        if (iso.isNullOrBlank()) return "—"
        return try {
            val inFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            val outFmt = SimpleDateFormat("dd MMM yyyy", Locale.US)
            outFmt.format(inFmt.parse(iso)!!)
        } catch (e: Exception) {
            iso
        }
    }
}
