package com.vectorpeaks.edulink.ui.screens.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.network.RetrofitClient
import com.vectorpeaks.edulink.security.AuthPreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val user: User) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    fun loadUser(userId: Int) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val user = RetrofitClient.apiService.getUserById(userId)
                _uiState.value = ProfileUiState.Success(user)
            } catch (e: HttpException) {
                _uiState.value = ProfileUiState.Error("HTTP error: ${e.code()}")
            } catch (e: IOException) {
                _uiState.value = ProfileUiState.Error("Network error")
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("Unexpected error")
            }
        }
    }

    fun updateUser(userId: Int, updatedUser: User, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = RetrofitClient.apiService.updateUser(userId, updatedUser)
                _uiState.value = ProfileUiState.Success(result)
                onSuccess()
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorMsg = if (e.code() == 400 && errorBody != null) errorBody
                else "Update failed: ${e.message}"
                _uiState.value = ProfileUiState.Error(errorMsg)
            }
        }
    }

    fun deleteAccount(userId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val response = RetrofitClient.apiService.deleteUser(userId)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    _uiState.value = ProfileUiState.Error("Błąd usuwania: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("Błąd: ${e.message}")
            }
        }
    }

    /**
     * Sends a logout request to the backend (invalidates the refresh token + clears FCM),
     * then clears local data and triggers the navigation callback.
     */
    fun logout(context: Context, onComplete: () -> Unit) {
        val authPrefs    = AuthPreferencesManager(context)
        val refreshToken = authPrefs.getRefreshToken()
        val userId       = authPrefs.getUserId()
        val fcmToken     = authPrefs.getFcmToken()

        Timber.d("LOGOUT: refreshToken=$refreshToken, userId=$userId")

        viewModelScope.launch {
            try {
                if (!refreshToken.isNullOrBlank()) {
                    val response = RetrofitClient.apiService.logout(
                        mapOf(
                            "refreshToken" to (refreshToken ?: ""),
                            "userId"       to userId.toString(),
                            "fcmToken"     to (fcmToken ?: "")
                        )
                    )
                    Timber.d("LOGOUT: backend response = ${response.code()}")
                } else {
                    Timber.w("LOGOUT: brak refresh tokena — nie wysyłam do backendu")
                }
            } catch (e: Exception) {
                Timber.e(e, "LOGOUT: wyjątek przy wylogowaniu")
            } finally {
                Timber.d("LOGOUT: czyszczę authPrefs")
                authPrefs.clearAll()
                onComplete()
            }
        }
    }

    /**
     * Usuwa konto z backendu, następnie czyści lokalną sesję.
     */
    fun deleteAccount(userId: Int, context: Context, onSuccess: () -> Unit) {
        val authPrefs = AuthPreferencesManager(context)
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val response = RetrofitClient.apiService.deleteUser(userId)
                if (response.isSuccessful) {
                    authPrefs.clearAll() // Wyczyść token, refresh token, userId
                    onSuccess()
                } else {
                    _uiState.value = ProfileUiState.Error("Błąd usuwania: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("Błąd: ${e.message}")
            }
        }
    }

    private val _showAddress = MutableStateFlow(false)
    val showAddress: StateFlow<Boolean> = _showAddress

    fun updateShowAddress(value: Boolean) {
        _showAddress.value = value
    }

}