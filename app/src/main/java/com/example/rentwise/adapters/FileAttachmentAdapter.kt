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
        //Function to bind all files to the appropriate layout file for the recycler view
        fun bind(uri: Uri) {
            val context = binding.root.context
            var fileName: String? = null

            // Attempt to retrieve the file name from the content resolver if the URI scheme is "content"
            if (uri.scheme == "content") {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME) // Get the index of the display name column
                        // If the index is valid, retrieve the file name
                        if (index != -1) fileName = cursor.getString(index)
                    }
                }
            }

            // If the file name couldn't be retrieved, extract it from the URI path
            if (fileName == null) {
                fileName = uri.path?.substringAfterLast('/') ?: "unknown_file"
            }

            binding.tvFileName.text = fileName

            //On click listener to remove a file from the recyclerview at the click position
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
        holder.bind(files[position]) //call the bind function
    }

    override fun getItemCount(): Int = files.size
}
