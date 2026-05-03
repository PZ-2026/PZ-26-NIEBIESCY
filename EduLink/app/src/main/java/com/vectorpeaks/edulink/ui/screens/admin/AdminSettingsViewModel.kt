package com.vectorpeaks.edulink.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _subjects = MutableStateFlow<List<String>>(emptyList())
    val subjects: StateFlow<List<String>> = _subjects

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

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
                val result = RetrofitClient.apiService.getSubjects()
                _subjects.value = result
            } catch (e: Exception) {

            }
        }
    }

    fun saveSettings(maxPrice: Double, message: String) {
        viewModelScope.launch {
            _saveSuccess.value = false
            try {
                val dto = GlobalLimitDto(maxPricePerHour = maxPrice, globalMessage = message)
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

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }
}
