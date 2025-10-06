package com.example.rentwise.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.rentwise.databinding.ItemSelectFileBinding

// Adapter for displaying a list of file attachments in a RecyclerView.
// Each file is represented by its Uri, and users can remove files via a delete button.
class FileAttachmentAdapter(
    private val files: List<Uri>,
    private val onDeleteClick: (position: Int) -> Unit
) : RecyclerView.Adapter<FileAttachmentAdapter.FileViewHolder>() {

    // ViewHolder class responsible for binding file data to the corresponding layout.
    inner class FileViewHolder(val binding: ItemSelectFileBinding) : RecyclerView.ViewHolder(binding.root) {
        // Binds a file Uri to the layout, displaying its name and handling delete actions.
        fun bind(uri: Uri) {
            val context = binding.root.context
            var fileName: String? = null

            // Attempts to retrieve the file name using the content resolver if the Uri uses the "content" scheme.
            // This is common for files selected from storage providers or other apps.
            if (uri.scheme == "content") {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        // Retrieves the index of the display name column, which holds the file name.
                        val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        // If the index is valid, extracts the file name from the cursor.
                        if (index != -1) fileName = cursor.getString(index)
                    }
                }
            }

            // If the file name could not be determined from the content resolver,
            // extracts the file name from the Uri path as a fallback.
            if (fileName == null) {
                fileName = uri.path?.substringAfterLast('/') ?: "unknown_file"
            }

            // Sets the extracted or fallback file name to the TextView for display.
            binding.tvFileName.text = fileName

            // Sets up a click listener on the delete button to trigger the provided callback,
            // allowing the user to remove the file at the current adapter position.
            binding.btnDeleteFile.setOnClickListener {
                onDeleteClick(adapterPosition)
            }
        }
    }

    // Inflates the layout for each file item and creates a new ViewHolder.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemSelectFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    // Binds the file Uri at the given position to the ViewHolder for display.
    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    // Returns the total number of file items to be displayed in the RecyclerView.
    override fun getItemCount(): Int = files.size
}
