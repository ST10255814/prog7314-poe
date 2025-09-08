package com.example.rentwise.shared_pref_config

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.core.content.edit

//https://youtu.be/2uResVLUCNI?si=pOnkbX8vZr0fATx1
class TokenManger (context: Context){

    private val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPref = EncryptedSharedPreferences.create(
        "secret_prefs",
        masterKey,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String){
        sharedPref.edit { putString("jwt_token", token) }
    }
    fun getToken() : String? {
        return sharedPref.getString("jwt_token", null)
    }
    fun clearToken() {
        sharedPref.edit { clear() }
    }
}