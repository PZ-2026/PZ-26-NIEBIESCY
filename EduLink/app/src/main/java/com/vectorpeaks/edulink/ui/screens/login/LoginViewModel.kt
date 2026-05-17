/*
 * LoginViewModel.kt
 *
 * Version: 1.1
 * Date: 2026-05-11
 *
 */

package com.vectorpeaks.edulink.ui.screens.login

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.vectorpeaks.edulink.data.model.LoginRequest
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.data.model.user.UserResponse
import com.vectorpeaks.edulink.network.RetrofitClient
import com.vectorpeaks.edulink.service.EduLinkFirebaseMessagingService
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
 * ViewModel for the login screen.
 *
 * Extends [AndroidViewModel] (instead of plain [ViewModel]) so it has access
 * to [Application] context — required for writing to SharedPreferences from
 * within the ViewModel without leaking an Activity context.
 *
 * @param application the application context injected automatically by the framework
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    /**
     * Authenticates the user with the given credentials, registers the device's
     * FCM token on the backend, and persists the user ID to SharedPreferences
     * so [EduLinkFirebaseMessagingService] can re-send the token if it is refreshed
     * while the user is already logged in.
     *
     * FCM registration failures are non-fatal — the user is still logged in and
     * the token will be re-sent on the next successful login.
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

                // Save userId so EduLinkFirebaseMessagingService can read it on token refresh
                getApplication<Application>()
                    .getSharedPreferences(EduLinkFirebaseMessagingService.PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putInt(EduLinkFirebaseMessagingService.KEY_USER_ID, user.id)
                    .apply()

                // Register FCM token — non-fatal if Firebase is unavailable
                try {
                    val token = FirebaseMessaging.getInstance().token.await()
                    Timber.d("FCM token obtained: $token")
                    RetrofitClient.apiService.updateFcmToken(user.id, mapOf("fcmToken" to token))
                    Timber.d("FCM token linked to user ${user.id} on backend")
                } catch (fcmException: Exception) {
                    Timber.e(fcmException, "FCM registration failed — notifications may not work")
                }

                _uiState.value = LoginUiState.Success(user)

            } catch (e: HttpException) {
                val errorMsg = if (e.code() == 401) "Nieprawidłowy email lub hasło"
                else "Błąd serwera (${e.code()})"
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
     * Call this after navigating away from the login screen to avoid
     * re-triggering the success/error state on recomposition.
     */
    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}