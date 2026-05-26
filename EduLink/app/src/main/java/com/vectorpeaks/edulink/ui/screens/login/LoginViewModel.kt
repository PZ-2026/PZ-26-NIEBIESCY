package com.vectorpeaks.edulink.ui.screens.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.vectorpeaks.edulink.data.model.LoginRequest
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.data.model.user.UserResponse
import com.vectorpeaks.edulink.network.RetrofitClient
import com.vectorpeaks.edulink.security.AuthPreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

sealed class LoginUiState {
    object Idle    : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User)    : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

/**
 * ViewModel responsible for handling the user authentication flow.
 * Uses [AuthPreferencesManager] to securely persist credentials.
 *
 * @version 1.2
 * @author EduLink Team
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    // Injecting our secure storage manager
    private val authPrefs = AuthPreferencesManager(application)

    /**
     * Authenticates the user, saves the issued JWT token and User ID securely,
     * and registers the device's FCM token on the backend.
     *
     * @param email    the user's email address
     * @param password the user's plain-text password
     */
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Proszę wypełnić wszystkie pola")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val request  = LoginRequest(email.trim(), password)
                val response: UserResponse = RetrofitClient.apiService.login(request)

                // This allows AuthInterceptor to automatically sign future API requests
                authPrefs.saveUserId(response.id)
                authPrefs.saveRefreshToken(response.refreshToken)
                authPrefs.saveToken(response.token)

                val roleId = response.role.toIntOrNull() ?: 3
                val user = User(
                    id              = response.id,
                    roleId          = roleId,
                    password        = null,
                    firstName       = response.firstName,
                    lastName        = response.lastName,
                    email           = response.email,
                    accountStatusId = 1,
                    address         = response.address     ?: "",
                    phoneNumber     = response.phoneNumber ?: ""
                )

                // register fcm token (Non-fatal if Firebase services fail)
                try {
                    val token = FirebaseMessaging.getInstance().token.await()
                    authPrefs.saveFcmToken(token)
                    Timber.d("FCM token obtained: $token")
                    RetrofitClient.apiService.updateFcmToken(user.id, mapOf("fcmToken" to token))
                    Timber.d("FCM token linked to user ${user.id} on backend")
                } catch (fcmException: Exception) {
                    Timber.e(fcmException, "FCM registration failed — background notifications will be disabled")
                }

                _uiState.value = LoginUiState.Success(user)

            } catch (e: HttpException) {
                val errorMsg = when (e.code()) {
                    401 -> "Nieprawidłowy email lub hasło"
                    403 -> "Twoje konto jest zablokowane"
                    503 -> "Trwają prace serwisowe"
                    else -> "Błąd serwera (${e.code()})"
                }
                _uiState.value = LoginUiState.Error(errorMsg)
            } catch (e: IOException) {
                _uiState.value = LoginUiState.Error("Błąd sieci – sprawdź połączenie")
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("Nieoczekiwany błąd: ${e.message}")
            }
        }
    }

    /**
     * Resets the UI state back to [LoginUiState.Idle].
     */
    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}