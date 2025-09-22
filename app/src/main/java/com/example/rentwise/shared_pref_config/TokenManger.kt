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
    fun saveUser(userId: String){
        sharedPref.edit { putString("user_id", userId)}
    }
    fun saveGooglePfp(photo: String) {
        sharedPref.edit { putString("google_photo", photo) }
    }
    fun savePfp(photo: String) {
        sharedPref.edit { putString("pfp_photo", photo) }
    }
    fun getToken() : String? {
        return sharedPref.getString("jwt_token", null)
    }
    fun getUser() : String? {
        return sharedPref.getString("user_id", null)
    }
    fun getGooglePfp() : String? {
        return sharedPref.getString("google_photo", null)
    }
    fun getPfp() : String? {
        return sharedPref.getString("pfp_photo", null)
    }
    fun clearToken() {
        sharedPref.edit { remove("jwt_token") }
    }
    fun clearUser(){
        sharedPref.edit { remove("user_id") }
    }
    fun clearGooglePfp() {
        sharedPref.edit { remove("google_photo") }
    }
    fun clearPfp() {
        sharedPref.edit { remove("pfp_photo") }
    }
}