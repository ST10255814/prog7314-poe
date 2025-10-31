package com.example.rentwise.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.Locale

// <------THIS WAS CHANGED-----> NEW UTILITY CLASS FOR MANAGING APP LANGUAGE/LOCALE CHANGES
// This helper class manages locale changes across the app, ensuring consistent language switching
// Based on the functioning language change example provided
class LocaleHelper {

    companion object {
        private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

        // <------THIS WAS CHANGED-----> METHOD TO CHANGE APP LOCALE/LANGUAGE
        // Changes the app's locale and updates the configuration
        // This method is called when user selects a different language from settings
        fun setLocale(context: Context, languageCode: String): Context {
            persist(context, languageCode)

            // For API 24 and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return updateResources(context, languageCode)
            }

            // For older API versions
            return updateResourcesLegacy(context, languageCode)
        }

        // <------THIS WAS CHANGED-----> PERSIST SELECTED LANGUAGE TO SHARED PREFERENCES
        // Saves the selected language code to SharedPreferences for persistence across app restarts
        private fun persist(context: Context, languageCode: String) {
            val preferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
            preferences.edit().putString(SELECTED_LANGUAGE, languageCode).apply()
        }

        // <------THIS WAS CHANGED-----> RETRIEVE SAVED LANGUAGE FROM SHARED PREFERENCES
        // Gets the previously saved language code, defaults to "en" if none saved
        fun getPersistedLanguage(context: Context): String {
            val preferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
            return preferences.getString(SELECTED_LANGUAGE, "en") ?: "en"
        }

        // <------THIS WAS CHANGED-----> UPDATE RESOURCES FOR API 24+
        // Updates app resources with the new locale for Android N and above
        private fun updateResources(context: Context, languageCode: String): Context {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val configuration = Configuration(context.resources.configuration)
            configuration.setLocale(locale)
            configuration.setLayoutDirection(locale)

            return context.createConfigurationContext(configuration)
        }

        // <------THIS WAS CHANGED-----> UPDATE RESOURCES FOR OLDER API VERSIONS
        // Updates app resources with the new locale for pre-Android N devices
        @Suppress("DEPRECATION")
        private fun updateResourcesLegacy(context: Context, languageCode: String): Context {
            val locale = Locale(languageCode)
            Locale.setDefault(locale)

            val resources: Resources = context.resources
            val configuration: Configuration = resources.configuration
            configuration.locale = locale
            configuration.setLayoutDirection(locale)

            resources.updateConfiguration(configuration, resources.displayMetrics)

            return context
        }

        // <------THIS WAS CHANGED-----> APPLY LOCALE TO ACTIVITY
        // Applies the saved locale to an activity, used in onCreate of activities
        fun onAttach(context: Context): Context {
            val lang = getPersistedLanguage(context)
            return setLocale(context, lang)
        }

        // <------THIS WAS CHANGED-----> RESTART ACTIVITY TO APPLY LANGUAGE CHANGE
        // Restarts the current activity to apply language changes immediately
        fun restartActivity(activity: Activity) {
            val intent = activity.intent
            activity.finish()
            activity.startActivity(intent)
        }
    }
}