package com.example.rentwise.custom_toast

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.rentwise.R

// Utility class for displaying visually distinct toast notifications with custom icons and messages.
//Enhanced to support both string resources and direct strings for proper internationalization.
class CustomToast {

    companion object {

        // Displays a custom toast with a message from string resource
        fun show(context: Context, messageResId: Int, type: ToastType, vararg formatArgs: Any) {
            val message = if (formatArgs.isNotEmpty()) {
                context.getString(messageResId, *formatArgs)
            } else {
                context.getString(messageResId)
            }
            show(context, message, type)
        }

        // Displays a custom toast with a specific message and icon based on the notification type.
        fun show(context: Context, message: String, type: ToastType) {
            // Inflates the custom layout for the toast, allowing for personalized appearance.
            val inflater = LayoutInflater.from(context)
            val layout: View = inflater.inflate(R.layout.custom_toast, null)

            // Retrieves the text and icon views from the custom layout for dynamic content updates.
            val toastText = layout.findViewById<TextView>(R.id.toast_text)
            val toastIcon = layout.findViewById<ImageView>(R.id.toast_icon)

            // Sets the message to be displayed in the toast.
            toastText.text = message

            // Selects the appropriate icon resource based on the toast type for visual feedback.
            when (type) {
                ToastType.SUCCESS -> toastIcon.setImageResource(R.drawable.ic_success)
                ToastType.ERROR -> toastIcon.setImageResource(R.drawable.ic_error)
                ToastType.INFO -> toastIcon.setImageResource(R.drawable.ic_info)
            }

            // Configures and displays the toast at the bottom center of the screen with a slight vertical offset.
            with(Toast(context)) {
                duration = Toast.LENGTH_SHORT
                view = layout
                setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
                show()
            }
        }

        // Enum representing the types of toast notifications for consistent icon selection.
        enum class ToastType {
            SUCCESS, ERROR, INFO
        }
    }
}
