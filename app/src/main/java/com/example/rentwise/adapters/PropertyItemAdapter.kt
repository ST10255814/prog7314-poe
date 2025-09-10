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

class PropertyItemAdapter(
    private val properties: List<ListingResponse>,
    private val onItemClick: (ListingResponse) -> Unit
) : RecyclerView.Adapter<PropertyItemAdapter.PropertyViewHolder>() {

    class PropertyViewHolder(val binding: ItemPropertyCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val binding = ItemPropertyCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PropertyViewHolder(binding)
    }

    override fun getItemCount(): Int = properties.size

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = properties[position]
        val amenitiesLabels = listOf(
            holder.binding.tvLabel1,
            holder.binding.tvLabel2,
            holder.binding.tvLabel3,
            holder.binding.tvLabel4
        )

        with(holder.binding) {
            val firstImage = property.imagesURL?.firstOrNull()
            Glide.with(imageProperty.context)
                .load(firstImage ?: R.drawable.ic_empty)
                .placeholder(R.drawable.ic_empty)
                .error(R.drawable.ic_empty)
                .into(imageProperty)

            tvTitle.text = property.title ?: "No Title"
            tvAddress.text = property.address ?: "No Address"

            amenitiesLabels.forEach { it.visibility = View.GONE }

            property.amenities?.take(4)?.forEachIndexed { index, amenity ->
                amenitiesLabels[index].text = amenity
                amenitiesLabels[index].visibility = View.VISIBLE
            }

            tvPrice.text = property.price?.let { "R$it" } ?: "Price N/A"
        }

        holder.binding.root.setOnClickListener { onItemClick(property) }

        holder.binding.root.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
            }
            false
        }
    }
}
