package com.example.rentwise.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rentwise.R

class FileAttachmentAdapter(
    private val filesAttached: MutableList<String>,
    private val onDeleteClick: (position: Int) -> Unit
) : RecyclerView.Adapter<FileAttachmentAdapter.FileViewHolder>() {

    class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.tv_file_name)
        val deleteBtn: ImageButton = itemView.findViewById(R.id.btn_delete_file)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_select_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = filesAttached[position]
        holder.fileName.text = file

        holder.deleteBtn.setOnClickListener {
            onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int = filesAttached.size
}
