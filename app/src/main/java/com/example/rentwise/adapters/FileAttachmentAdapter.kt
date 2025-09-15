package com.example.rentwise.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rentwise.databinding.ItemSelectFileBinding

class FileAttachmentAdapter(
    private val files: List<Uri>,
    private val onDeleteClick: (position: Int) -> Unit
) : RecyclerView.Adapter<FileAttachmentAdapter.FileViewHolder>() {

    inner class FileViewHolder(val binding: ItemSelectFileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(uri: Uri) {
            val context = binding.root.context
            var fileName: String? = null

            if (uri.scheme == "content") {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (index != -1) fileName = cursor.getString(index)
                    }
                }
            }

            if (fileName == null) {
                fileName = uri.path?.substringAfterLast('/') ?: "unknown_file"
            }

            binding.tvFileName.text = fileName

            binding.btnDeleteFile.setOnClickListener {
                onDeleteClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemSelectFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount(): Int = files.size
}
