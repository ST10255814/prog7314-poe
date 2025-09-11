package com.example.rentwise.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rentwise.R
import com.example.rentwise.data_classes.FavouriteListingsResponse
import com.example.rentwise.databinding.WishlistItemBinding

class WishlistAdapter(
    val wishlistProperties: MutableList<FavouriteListingsResponse>,
    private val onItemClick: (FavouriteListingsResponse) -> Unit,
    private val onUnFavouriteClick: (FavouriteListingsResponse, Int) -> Unit
) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    class WishlistViewHolder(val binding: WishlistItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val binding = WishlistItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return WishlistViewHolder(binding)
    }

    override fun getItemCount(): Int = wishlistProperties.size

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        val wishlistItem = wishlistProperties[position]

        with(holder.binding){
            val firstImage = wishlistItem.listingDetail?.images?.firstOrNull()
            Glide.with(propertyImage.context)
                .load(firstImage ?: R.drawable.ic_empty)
                .placeholder(R.drawable.ic_empty)
                .error(R.drawable.ic_empty)
                .into(propertyImage)

            propertyTitle.text = wishlistItem.listingDetail?.title ?: "No Title"
            propertyLocation.text = wishlistItem.listingDetail?.address ?: "No address"
            propertyAmount.text = wishlistItem.listingDetail?.price.let { "R${it}" } ?: "Price N/A"

            root.setOnClickListener { onItemClick(wishlistItem) }

            root.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> v.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
                false
            }

            favouriteIcon.setOnClickListener {
                val currentPosition = holder.adapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    ImageViewCompat.setImageTintList(
                        favouriteIcon,
                        ContextCompat.getColorStateList(holder.itemView.context, R.color.grey)
                    )
                    favouriteIcon.setImageResource(R.drawable.favourite_icon)

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

    fun removeAt(position: Int) {
        if (position in wishlistProperties.indices) {
            wishlistProperties.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
