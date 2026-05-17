package com.vectorpeaks.edulink.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Secure storage for authentication credentials using EncryptedSharedPreferences.
 * All values are encrypted with AES256.
 *
 * @version 1.1
 * @author EduLink Team
 */
class AuthPreferencesManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // Jedna prywatna referencja — używana wszędzie niżej
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "auth_prefs_secure",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // ── Access token (JWT, 15 min) ───────────────────────────────

    fun saveToken(token: String) =
        encryptedPrefs.edit().putString("jwt_token", token).apply()

    fun getToken(): String? =
        encryptedPrefs.getString("jwt_token", null)

    // ── Refresh token (7 dni, przechowywany w bazie backendu) ───

    fun saveRefreshToken(token: String) =
        encryptedPrefs.edit().putString("refresh_token", token).apply()

    fun getRefreshToken(): String? =
        encryptedPrefs.getString("refresh_token", null)

    // ── User ID ─────────────────────────────────────────────────

    fun saveUserId(userId: Int) =
        encryptedPrefs.edit().putInt("user_id", userId).apply()

    fun getUserId(): Int =
        encryptedPrefs.getInt("user_id", -1)

    // ── Logout — czyści wszystkie dane ──────────────────────────

    fun clearAll() =
        encryptedPrefs.edit().clear().apply()

    fun saveFcmToken(token: String) =
        encryptedPrefs.edit().putString("fcm_token", token).apply()

    fun getFcmToken(): String? =
        encryptedPrefs.getString("fcm_token", null)
}