package com.example.rentwise.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rentwise.R
import com.example.rentwise.data_classes.ListingResponse
import com.example.rentwise.databinding.ItemPropertyCardBinding

// Adapter for displaying a list of property items in a RecyclerView.
// Each property card shows an image, title, address, amenities, and price, and supports click and touch animations.
class PropertyItemAdapter(
    private var properties: List<ListingResponse>,
    private val onItemClick: (ListingResponse) -> Unit
) : RecyclerView.Adapter<PropertyItemAdapter.PropertyViewHolder>() {

    // ViewHolder class that holds the binding for each property card layout.
    class PropertyViewHolder(val binding: ItemPropertyCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    // Inflates the property card layout and creates a new ViewHolder for each property.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val binding = ItemPropertyCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PropertyViewHolder(binding)
    }

    // Returns the total number of property items to be displayed in the RecyclerView.
    override fun getItemCount(): Int = properties.size

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    // Binds the property data to the ViewHolder, populating all relevant fields and handling UI interactions.
    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = properties[position] // Retrieves the property at the current position.

        // Prepares a list of TextViews for displaying up to four amenities.
        val amenitiesLabels = listOf(
            holder.binding.tvLabel1,
            holder.binding.tvLabel2,
            holder.binding.tvLabel3,
            holder.binding.tvLabel4
        )

        // Binds property details to the UI components.
        with(holder.binding) {
            // Loads the first image URL using Glide, or a placeholder if unavailable.
            val firstImage = property.imagesURL?.firstOrNull()
            Glide.with(imageProperty.context)
                .load(firstImage ?: R.drawable.ic_empty)
                .placeholder(R.drawable.ic_empty)
                .error(R.drawable.ic_empty)
                .into(imageProperty)

            // Sets the property title or a default if missing.
            tvTitle.text = property.title ?: "No Title"

            // Sets the property address (show full address; add area if present).
            // This ensures the card displays the actual address as requested.
            val address = property.address?.takeIf { it.isNotBlank() }
            val area = property.area?.takeIf { it.isNotBlank() }
            tvAddress.text = when {
                address != null && area != null -> "$address · $area"
                address != null -> address
                area != null -> area
                else -> "No Address"
            }

            // Hides all amenities labels initially.
            amenitiesLabels.forEach { it.visibility = View.GONE }

            // Displays up to four amenities, making the corresponding labels visible.
            property.amenities?.take(4)?.forEachIndexed { index, amenity ->
                amenitiesLabels[index].text = amenity
                amenitiesLabels[index].visibility = View.VISIBLE
            }

            // Sets the property price with formatting, or a default if missing.
            tvPrice.text = property.price?.let { "R$it" } ?: "Price N/A"

            // Rating on card
            val rating = property.averageRating
            if (rating != null && rating > 0f) {
                cardRatingCluster.visibility = View.VISIBLE
                tvCardRating.text = String.format("%.1f", rating)
            } else {
                cardRatingCluster.visibility = View.GONE
            }
        }

        // Sets up a click listener for the property card, triggering the provided callback.
        holder.binding.root.setOnClickListener { onItemClick(property) }

        // Adds a touch animation to the property card for enhanced user experience.
        holder.binding.root.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
            false
        }
    }

    // Updates the displayed property list with a filtered list and refreshes the RecyclerView.
    @SuppressLint("NotifyDataSetChanged")
    fun updateListViaFilters(filteredList: List<ListingResponse>) {
        properties = filteredList
        notifyDataSetChanged()
    }
}
