// AutoLoginScreen.kt
package com.vectorpeaks.edulink.ui.screens.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.network.RetrofitClient
import com.vectorpeaks.edulink.security.AuthPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber

/**
 * Invisible transition screen shown at app start when a refresh token exists.
 *
 * Flow:
 * 1. Sends refresh token to backend → gets new access token
 * 2. Fetches user data with new token
 * 3. Navigates to correct main screen (Student/Tutor/Admin)
 * 4. On any failure → navigates to Login
 *
 * @version 1.1
 * @author EduLink Team
 */
@Composable
fun AutoLoginScreen(
    authPrefs: AuthPreferencesManager,
    onSuccess: (User) -> Unit,
    onFailure: () -> Unit
) {
    // LaunchedEffect runs on the Main dispatcher — navigation callbacks are safe
    LaunchedEffect(Unit) {
        val succeeded = tryAutoLogin(authPrefs)
        if (succeeded != null) {
            onSuccess(succeeded)
        } else {
            onFailure()
        }
    }

    Box(
        modifier         = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Executes the auto-login logic outside of the composable.
 * Returns User upon success, null upon failure.
 * Network operations run on Dispatchers.IO, result returns to Main.
 */
private suspend fun tryAutoLogin(authPrefs: AuthPreferencesManager): User? {
    Timber.d("AutoLogin: Starting token verification procedure...")
    Timber.d("AUTOLOGIN: refreshToken po after start = ${authPrefs.getRefreshToken()}")
    return try {
        val refreshToken = authPrefs.getRefreshToken()
        if (refreshToken == null) {
            Timber.d("AutoLogin: No refresh token found in device memory.")
            return null
        }
        Timber.d("AutoLogin: Refresh token found. Sending refresh request...")

        // Network on IO
        val refreshResponse = withContext(Dispatchers.IO) {
            RetrofitClient.apiService.refreshToken(
                mapOf("refreshToken" to refreshToken)
            )
        }

        if (!refreshResponse.isSuccessful) {
            Timber.w("AutoLogin: Token refresh failed! HTTP Code: ${refreshResponse.code()}")
            authPrefs.clearAll()
            return null
        }

        val body = refreshResponse.body()?.string()
        if (body == null) {
            Timber.w("AutoLogin: Server response body is empty.")
            return null
        }

        val newToken = JSONObject(body).getString("token")
        authPrefs.saveToken(newToken)
        Timber.d("AutoLogin: New access token saved successfully.")

        val userId = authPrefs.getUserId()
        Timber.d("AutoLogin: Retrieved userId from preferences: $userId")
        if (userId == -1) {
            Timber.w("AutoLogin: Invalid userId (-1). Clearing data.")
            authPrefs.clearAll()
            return null
        }

        Timber.d("AutoLogin: Fetching profile data for user ID: $userId...")
        // Fetch user data and assign to variable
        val user = withContext(Dispatchers.IO) {
            RetrofitClient.apiService.getUserById(userId)
        }

        Timber.d("AutoLogin: Success! Profile fetched for: ${user.fullName}")
        return user // Explicit return of the user object

    } catch (e: Exception) {
        // Here you will find out if e.g. JSONObject threw an error or network failed
        Timber.e(e, "AutoLogin: A critical exception occurred during auto-login!")
        authPrefs.clearAll()
        null
    }
}