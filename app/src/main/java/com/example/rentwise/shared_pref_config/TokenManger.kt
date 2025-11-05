package com.example.rentwise.shared_pref_config

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.core.content.edit
import org.json.JSONObject

// Securely manages sensitive user data such as authentication tokens, user IDs, and profile photos using encrypted shared preferences.
class TokenManger(context: Context) {

    private val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPref = EncryptedSharedPreferences.create(
        "secret_prefs",
        masterKey,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Keys
    private val KEY_JWT = "jwt_token"
    private val KEY_USER = "user_id"
    private val KEY_PFP = "pfp_photo"
    private val KEY_BIOMETRIC_CT = "biometric_ciphertext"
    private val KEY_BIOMETRIC_IV = "biometric_iv"

    // Save single values (async by default)
    fun saveToken(token: String, commit: Boolean = false) {
        if (commit) sharedPref.edit(commit = true) { putString(KEY_JWT, token) }
        else sharedPref.edit { putString(KEY_JWT, token) }
    }

    fun saveUser(userId: String, commit: Boolean = false) {
        if (commit) sharedPref.edit(commit = true) { putString(KEY_USER, userId) }
        else sharedPref.edit { putString(KEY_USER, userId) }
    }

    fun savePfp(photo: String, commit: Boolean = false) {
        if (commit) sharedPref.edit(commit = true) { putString(KEY_PFP, photo) }
        else sharedPref.edit { putString(KEY_PFP, photo) }
    }

    // Synchronous atomic save for token + user + pfp (use this after biometric restore)
    fun saveAllSync(token: String?, userId: String?, pfp: String?) {
        sharedPref.edit(commit = true) {
            if (token != null) putString(KEY_JWT, token)
            if (userId != null) putString(KEY_USER, userId)
            if (pfp != null) putString(KEY_PFP, pfp)
        }
    }

    fun getToken(): String? = sharedPref.getString(KEY_JWT, null)
    fun getUser(): String? = sharedPref.getString(KEY_USER, null)
    fun getPfp(): String? = sharedPref.getString(KEY_PFP, null)

    fun hasToken(): Boolean = getToken() != null
    fun hasUser(): Boolean = getUser() != null

    fun clearToken() {
        sharedPref.edit { remove(KEY_JWT) }
    }

    fun clearUser() {
        sharedPref.edit { remove(KEY_USER) }
    }

    fun clearPfp() {
        sharedPref.edit { remove(KEY_PFP) }
    }

    // Biometric storage helpers
    fun saveEncryptedToken(ctBase64: String, ivBase64: String, commit: Boolean = false) {
        if (commit) {
            sharedPref.edit(commit = true) {
                putString(KEY_BIOMETRIC_CT, ctBase64)
                putString(KEY_BIOMETRIC_IV, ivBase64)
            }
        } else {
            sharedPref.edit {
                putString(KEY_BIOMETRIC_CT, ctBase64)
                putString(KEY_BIOMETRIC_IV, ivBase64)
            }
        }
    }

    fun getEncryptedTokenPair(): Pair<String, String>? {
        val ct = sharedPref.getString(KEY_BIOMETRIC_CT, null)
        val iv = sharedPref.getString(KEY_BIOMETRIC_IV, null)
        return if (ct != null && iv != null) Pair(ct, iv) else null
    }

    fun clearEncryptedToken() {
        sharedPref.edit {
            remove(KEY_BIOMETRIC_CT)
            remove(KEY_BIOMETRIC_IV)
        }
    }

    fun createBiometricPayload(token: String, userId: String? = null, pfp: String? = null): String {
        // fallback to stored values if params are null/blank
        val finalUserId = userId?.takeIf { it.isNotBlank() } ?: getUser()
        val finalPfp = pfp?.takeIf { it.isNotBlank() } ?: getPfp()

        val json = JSONObject()
        json.put("token", token)
        if (!finalUserId.isNullOrEmpty()) json.put("userId", finalUserId)
        if (!finalPfp.isNullOrEmpty()) json.put("pfp", finalPfp)
        return json.toString()
    }

    fun parseBiometricPayload(payloadJson: String): Triple<String?, String?, String?> {
        return try {
            val json = JSONObject(payloadJson)
            val token = json.optString("token", null)
            val userId = json.optString("userId", null)
            val pfp = json.optString("pfp", null)
            Triple(token, userId, pfp)
        } catch (e: Exception) {
            Triple(null, null, null)
        }
    }
}