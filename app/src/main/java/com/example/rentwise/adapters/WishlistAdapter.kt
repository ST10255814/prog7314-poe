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
import com.example.rentwise.R
import com.example.rentwise.data_classes.PropertyData

class WishlistAdapter(
    private val wishlistProperties: MutableList<PropertyData>,
    private val onItemClick: (PropertyData) -> Unit,
    private val onUnFavouriteClick: (PropertyData, Int) -> Unit
) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {

    class WishlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageProperty: ImageView = itemView.findViewById(R.id.property_image)
        val tvTitle: TextView = itemView.findViewById(R.id.property_title)
        val tvAddress: TextView = itemView.findViewById(R.id.property_location)
        val tvPrice: TextView = itemView.findViewById(R.id.property_amount)
        val favouriteBtn: ImageButton = itemView.findViewById(R.id.favourite_icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.wishlist_item, parent, false)
        return WishlistViewHolder(view)
    }

    override fun getItemCount(): Int {
        return wishlistProperties.size
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        val wishlistItem = wishlistProperties[position]

        holder.imageProperty.setImageResource(wishlistItem.imageResId)
        holder.tvTitle.text = wishlistItem.title
        holder.tvAddress.text = wishlistItem.address
        holder.tvPrice.text = wishlistItem.price

        holder.itemView.setOnClickListener {
            onItemClick(wishlistItem)
        }

        holder.favouriteBtn.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                ImageViewCompat.setImageTintList(
                    holder.favouriteBtn,
                    ContextCompat.getColorStateList(holder.itemView.context, R.color.grey)
                )
                holder.favouriteBtn.setImageResource(R.drawable.favourite_icon)

                holder.favouriteBtn.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        holder.favouriteBtn.scaleX = 1f
                        holder.favouriteBtn.scaleY = 1f
                        holder.favouriteBtn.alpha = 1f
                        onUnFavouriteClick(wishlistItem, currentPosition)
                    }
                    .start()
            }
        }

        holder.itemView.setOnTouchListener { v, event ->
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
    }
}