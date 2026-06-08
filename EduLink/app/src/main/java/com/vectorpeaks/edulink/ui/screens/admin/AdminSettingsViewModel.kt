package com.vectorpeaks.edulink.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vectorpeaks.edulink.data.model.MaintenanceStatus
import com.vectorpeaks.edulink.data.model.SubjectDto
import com.vectorpeaks.edulink.data.model.user.GlobalLimitDto
import com.vectorpeaks.edulink.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AdminSettingsViewModel : ViewModel() {
    private val _settings = MutableStateFlow<GlobalLimitDto?>(null)
    val settings: StateFlow<GlobalLimitDto?> = _settings

    private val _subjects = MutableStateFlow<List<SubjectDto>>(emptyList())
    val subjects: StateFlow<List<SubjectDto>> = _subjects

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    private val _maintenanceStatus = MutableStateFlow(MaintenanceStatus())
    val maintenanceStatus: StateFlow<MaintenanceStatus> = _maintenanceStatus

    fun loadSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = RetrofitClient.apiService.getAdminSettings()
                _settings.value = result
            } catch (e: HttpException) {
                _error.value = "HTTP error: ${e.code()}"
            } catch (e: IOException) {
                _error.value = "Network error"
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadSubjects() {
        viewModelScope.launch {
            try {
                val result = RetrofitClient.apiService.getSubjectsWithId()
                _subjects.value = result
            } catch (e: Exception) {
                // Silently ignore – subjects list is non-critical
            }
        }
    }

    fun savePriceLimit(maxPrice: Double) {
        val settings = _settings.value
        saveSettings(
            maxPrice = maxPrice,
            message = settings?.globalMessage ?: "",
            enabled = settings?.globalMessageEnabled ?: false
        )
    }

    fun saveGlobalMessage(message: String, enabled: Boolean) {
        val settings = _settings.value
        saveSettings(
            maxPrice = settings?.maxPricePerHour ?: 200.0,
            message = message,
            enabled = enabled
        )
    }

    fun setGlobalMessageEnabled(enabled: Boolean, currentMessage: String) {
        val settings = _settings.value
        saveSettings(
            maxPrice = settings?.maxPricePerHour ?: 200.0,
            message = currentMessage,
            enabled = enabled
        )
    }

    private fun saveSettings(maxPrice: Double, message: String, enabled: Boolean) {
        viewModelScope.launch {
            _saveSuccess.value = false
            try {
                val dto = GlobalLimitDto(
                    maxPricePerHour = maxPrice,
                    globalMessage = message,
                    globalMessageEnabled = enabled
                )
                val response = RetrofitClient.apiService.updateAdminSettings(dto)
                if (response.isSuccessful) {
                    _saveSuccess.value = true
                    _settings.value = dto
                } else {
                    _error.value = "Błąd zapisu: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Błąd: ${e.message}"
            }
        }
    }

    /**
     * Adds a new subject with the given name and optional max price.
     */
    fun addSubject(name: String) {
        viewModelScope.launch {
            try {
                val body = mapOf("name" to name)
                val response = RetrofitClient.apiService.addSubject(body)
                if (response.isSuccessful) {
                    loadSubjects()
                } else {
                    _error.value = "Błąd dodawania: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Błąd: ${e.message}"
            }
        }
    }

    /**
     * Deletes a subject by its ID.
     */
    fun deleteSubject(id: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.deleteSubject(id)
                if (response.isSuccessful) {
                    loadSubjects()
                } else {
                    val errorBody = response.errorBody()?.string()
                    _error.value = if (errorBody.isNullOrBlank()) {
                        "Błąd usuwania: ${response.code()}"
                    } else {
                        errorBody
                    }
                }
            } catch (e: Exception) {
                _error.value = "Błąd: ${e.message}"
            }
        }
    }


    /**
     * Sends logout request to backend (invalidates refresh token + clears FCM),
     * then clears local session data.
     */
    fun logout(context: android.content.Context, onComplete: () -> Unit) {
        val authPrefs    = com.vectorpeaks.edulink.security.AuthPreferencesManager(context)
        val refreshToken = authPrefs.getRefreshToken()
        val userId       = authPrefs.getUserId()
        val fcmToken     = authPrefs.getFcmToken()

        timber.log.Timber.d("LOGOUT: refreshToken=$refreshToken, userId=$userId")

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
                    timber.log.Timber.d("LOGOUT: backend response = ${response.code()}")
                } else {
                    timber.log.Timber.w("LOGOUT: brak refresh tokena")
                }
            } catch (e: Exception) {
                timber.log.Timber.e(e, "LOGOUT: wyjątek")
            } finally {
                timber.log.Timber.d("LOGOUT: czyszczę authPrefs")
                authPrefs.clearAll()
                onComplete()
            }
        }
    }

    /**
     * Loads current maintenance mode status from the backend.
     */
    fun loadMaintenanceStatus() {
        viewModelScope.launch {
            try {
                val result = RetrofitClient.apiService.getMaintenanceStatus()
                _maintenanceStatus.value = result
            } catch (_: Exception) {
                // Silently ignore – maintenance status is non-critical
            }
        }
    }

    /**
     * Toggles maintenance mode on or off.
     */
    fun toggleMaintenance(active: Boolean) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.toggleMaintenance(mapOf("active" to active))
                if (response.isSuccessful) {
                    response.body()?.let { _maintenanceStatus.value = it }
                } else {
                    _error.value = "Błąd: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Błąd: ${e.message}"
            }
        }
    }

    /**
     * Shortens the maintenance mode cooldown to 1 minute.
     */
    fun shortenMaintenanceTime() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.toggleMaintenance(mapOf("force" to true))
                if (response.isSuccessful) {
                    response.body()?.let { _maintenanceStatus.value = it }
                } else {
                    _error.value = "Błąd: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Błąd: ${e.message}"
            }
        }
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }
}
