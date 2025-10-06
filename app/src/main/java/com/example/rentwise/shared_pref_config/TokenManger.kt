package com.example.rentwise.shared_pref_config

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.core.content.edit

// Securely manages sensitive user data such as authentication tokens, user IDs, and profile photos using encrypted shared preferences.
// Utilizes AndroidX Security library to ensure all stored values are encrypted at rest, protecting against unauthorized access.
//Mr.Code. 2020. Android Encrypted Shared Preferences - Android Tutorial (2020). [video online]
//Available at: <https://youtu.be/2uResVLUCNI?si=pOnkbX8vZr0fATx1> [Accessed 8 September 2025].
class TokenManger(context: Context) {

    // Generates or retrieves a master encryption key for securing the shared preferences.
    private val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    // Creates an instance of EncryptedSharedPreferences, specifying encryption schemes for both keys and values.
    private val sharedPref = EncryptedSharedPreferences.create(
        "secret_prefs", // Name of the encrypted preferences file.
        masterKey,      // Master key for encryption.
        context,        // Application context for file access.
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,   // Key encryption algorithm.
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM  // Value encryption algorithm.
    )

    // Stores the JWT authentication token securely in encrypted preferences.
    fun saveToken(token: String) {
        sharedPref.edit { putString("jwt_token", token) }
    }

    // Stores the user ID securely for session management.
    fun saveUser(userId: String) {
        sharedPref.edit { putString("user_id", userId) }
    }

    // Stores the profile photo URL or identifier securely.
    fun savePfp(photo: String) {
        sharedPref.edit { putString("pfp_photo", photo) }
    }

    // Retrieves the stored JWT token, or null if not present.
    fun getToken(): String? {
        return sharedPref.getString("jwt_token", null)
    }

    // Retrieves the stored user ID, or null if not present.
    fun getUser(): String? {
        return sharedPref.getString("user_id", null)
    }

    // Retrieves the stored profile photo, or null if not present.
    fun getPfp(): String? {
        return sharedPref.getString("pfp_photo", null)
    }

    // Removes the stored JWT token from encrypted preferences.
    fun clearToken() {
        sharedPref.edit { remove("jwt_token") }
    }

    // Removes the stored user ID from encrypted preferences.
    fun clearUser() {
        sharedPref.edit { remove("user_id") }
    }

    // Removes the stored profile photo from encrypted preferences.
    fun clearPfp() {
        sharedPref.edit { remove("pfp_photo") }
    }
}
