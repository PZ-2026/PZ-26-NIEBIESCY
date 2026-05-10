package com.vectorpeaks.edulink.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vectorpeaks.edulink.data.model.user.AdminReportsResponse
import com.vectorpeaks.edulink.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AdminReportsViewModel : ViewModel() {
    private val _reports = MutableStateFlow<AdminReportsResponse?>(null)
    val reports: StateFlow<AdminReportsResponse?> = _reports

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadReports() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = RetrofitClient.apiService.getAdminReports()
                _reports.value = result
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
}
