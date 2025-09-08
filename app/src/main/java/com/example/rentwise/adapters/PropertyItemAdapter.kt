package com.example.rentwise.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rentwise.R
import com.example.rentwise.data_classes.PropertyData
import com.example.rentwise.databinding.ItemPropertyCardBinding

class PropertyItemAdapter(
    private val properties: List<PropertyData>,
    private val onItemClick: (PropertyData) -> Unit
) : RecyclerView.Adapter<PropertyItemAdapter.PropertyViewHolder>(){

    class PropertyViewHolder (val binding: ItemPropertyCardBinding) :
        RecyclerView.ViewHolder(binding.root){
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val binding = ItemPropertyCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PropertyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return properties.size
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = properties[position]
        with(holder.binding){
            imageProperty.setImageResource(property.imageResId)
            tvTitle.text = property.title
            tvAddress.text = property.address
            tvLabel1.text = property.label1
            tvLabel2.text = property.label2
            tvPrice.text = property.price
        }

        holder.binding.root.setOnClickListener {
            onItemClick(property)
        }

        holder.binding.root.setOnTouchListener { v, event ->
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