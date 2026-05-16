package com.vectorpeaks.edulink.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AuthPreferencesManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "auth_prefs_secure",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) {
        encryptedPrefs.edit().putString("jwt_token", token).apply()
    }

    fun getToken(): String? {
        return encryptedPrefs.getString("jwt_token", null)
    }

    fun saveUserId(userId: Int) {
        encryptedPrefs.edit().putInt("user_id", userId).apply()
    }

    fun getUserId(): Int {
        return encryptedPrefs.getInt("user_id", -1)
    }

    fun clear() {
        encryptedPrefs.edit().clear().apply()
    }
}