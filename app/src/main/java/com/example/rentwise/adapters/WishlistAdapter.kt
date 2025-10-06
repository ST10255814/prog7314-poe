package com.example.rentwise.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rentwise.R
import com.example.rentwise.data_classes.FavouriteListingsResponse
import com.example.rentwise.databinding.WishlistItemBinding

// Adapter for displaying a list of properties in the user's wishlist within a RecyclerView.
// Each item shows property details, supports click actions, and allows users to remove properties from their wishlist.
class WishlistAdapter(
    val wishlistProperties: MutableList<FavouriteListingsResponse>,
    private val onItemClick: (FavouriteListingsResponse) -> Unit,
    private val onUnFavouriteClick: (FavouriteListingsResponse, Int) -> Unit
) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    // ViewHolder class that holds the binding for each wishlist item layout.
    class WishlistViewHolder(val binding: WishlistItemBinding) : RecyclerView.ViewHolder(binding.root)

    // Inflates the wishlist item layout and creates a new ViewHolder for each property.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val binding = WishlistItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WishlistViewHolder(binding)
    }

    // Returns the total number of properties in the wishlist to be displayed.
    override fun getItemCount(): Int = wishlistProperties.size

    @SuppressLint("ClickableViewAccessibility")
    // Binds the property data to the ViewHolder, populating all relevant fields and handling UI interactions.
    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        val wishlistItem = wishlistProperties[position] // Retrieves the property at the current position.

        // Binds property details to the UI components.
        with(holder.binding){
            // Loads the first image URL using Glide, or a placeholder if unavailable.
            val firstImage = wishlistItem.listingDetail?.images?.firstOrNull()
            Glide.with(propertyImage.context)
                .load(firstImage ?: R.drawable.ic_empty)
                .placeholder(R.drawable.ic_empty)
                .error(R.drawable.ic_empty)
                .into(propertyImage)

            // Sets the property title or a default if missing.
            propertyTitle.text = wishlistItem.listingDetail?.title ?: "No Title"
            // Sets the property address or a default if missing.
            propertyLocation.text = wishlistItem.listingDetail?.address ?: "No address"
            // Sets the property price with formatting, or a default if missing.
            propertyAmount.text = wishlistItem.listingDetail?.price.let { "R${it}" } ?: "Price N/A"

            // Sets up a click listener for the property card, triggering the provided callback.
            root.setOnClickListener { onItemClick(wishlistItem) }

            // Adds a touch animation to the property card for enhanced user experience.
            root.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
                false
            }

            // Handles the unfavourite action, animating the icon and triggering the removal callback.
            favouriteIcon.setOnClickListener {
                val currentPosition = holder.adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    // Changes the icon tint and image to indicate removal.
                    ImageViewCompat.setImageTintList(
                        favouriteIcon,
                        ContextCompat.getColorStateList(holder.itemView.context, R.color.grey)
                    )
                    favouriteIcon.setImageResource(R.drawable.favourite_icon)

                    // Animates the icon and calls the unfavourite callback after the animation.
                    favouriteIcon.animate()
                        .scaleX(0f)
                        .scaleY(0f)
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction {
                            favouriteIcon.scaleX = 1f
                            favouriteIcon.scaleY = 1f
                            favouriteIcon.alpha = 1f
                            onUnFavouriteClick(wishlistItem, currentPosition)
                        }
                        .start()
                }
            }
        }
    }

    // Removes a property from the wishlist at the specified position and updates the RecyclerView.
    fun removeAt(position: Int) {
        if (position in wishlistProperties.indices) {
            wishlistProperties.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
