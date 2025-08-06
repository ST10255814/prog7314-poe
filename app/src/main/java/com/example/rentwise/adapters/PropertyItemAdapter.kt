package com.example.rentwise.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rentwise.R
import com.example.rentwise.data_classes.PropertyData

class PropertyItemAdapter(private val properties: List<PropertyData>) :
    RecyclerView.Adapter<PropertyItemAdapter.PropertyViewHolder>(){

    class PropertyViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView){
        val imageProperty: ImageView = itemView.findViewById(R.id.image_property)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvAddress: TextView = itemView.findViewById(R.id.tv_address)
        val tvLabel1: TextView = itemView.findViewById(R.id.tv_label1)
        val tvLabel2: TextView = itemView.findViewById(R.id.tv_label2)
        val tvPrice: TextView = itemView.findViewById(R.id.tv_price)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_property_card, parent, false)
        return PropertyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return properties.size
    }

    override fun onBindViewHolder(holder: PropertyViewHolder, position: Int) {
        val property = properties[position]
        holder.imageProperty.setImageResource(property.imageResId)
        holder.tvTitle.text = property.title
        holder.tvAddress.text = property.address
        holder.tvLabel1.text = property.label1
        holder.tvLabel2.text = property.label2
        holder.tvPrice.text = property.price
    }
}