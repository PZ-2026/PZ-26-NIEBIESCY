package com.vectorpeaks.edulink.security

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.File

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

    private val encryptedPrefs: SharedPreferences = try {
        createEncryptedPrefs(context)
    } catch (e: Exception) {
        deleteCorruptedPrefs(context)
        createEncryptedPrefs(context)
    }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            "auth_prefs_secure",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun deleteCorruptedPrefs(context: Context) {
        try {
            context.getSharedPreferences("auth_prefs_secure", Context.MODE_PRIVATE).edit {
                clear()
            }

            val dir = File(context.filesDir.parent + "/shared_prefs/")
            val file = File(dir, "auth_prefs_secure.xml")
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

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