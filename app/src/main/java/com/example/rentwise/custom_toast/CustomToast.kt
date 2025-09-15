package com.example.rentwise.custom_toast

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.rentwise.R

// Followed a video tutorial to create a custom toast
// https://youtu.be/H9WF3te9Gdw?si=d96yB-zNUV61zhAL

class CustomToast {

    companion object {

        fun show(context: Context, message: String, type: ToastType) {
            // Inflate custom layout
            val inflater = LayoutInflater.from(context)
            val layout: View = inflater.inflate(R.layout.custom_toast, null)

            // Find views
            val toastText = layout.findViewById<TextView>(R.id.toast_text)
            val toastIcon = layout.findViewById<ImageView>(R.id.toast_icon)

            toastText.text = message

            // Set icon based on type
            when (type) {
                ToastType.SUCCESS -> toastIcon.setImageResource(R.drawable.ic_success)
                ToastType.ERROR -> toastIcon.setImageResource(R.drawable.ic_error)
                ToastType.INFO -> toastIcon.setImageResource(R.drawable.ic_info)
            }

            // Show toast at default bottom position
            with(Toast(context)) {
                duration = Toast.LENGTH_SHORT
                view = layout
                setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
                show()
            }
        }

        // Enum for toast types
        enum class ToastType {
            SUCCESS, ERROR, INFO
        }
    }
}
