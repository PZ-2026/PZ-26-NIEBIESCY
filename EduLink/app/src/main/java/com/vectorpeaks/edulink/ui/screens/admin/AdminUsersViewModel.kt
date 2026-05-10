package com.vectorpeaks.edulink.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AdminUsersViewModel : ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = RetrofitClient.apiService.getAllUsers()
                _users.value = result
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

    fun toggleUserBlock(userId: Int, currentStatusId: Int) {
        viewModelScope.launch {
            try {
                val newStatusId = if (currentStatusId == 1) 2 else 1
                val response = RetrofitClient.apiService.updateUserStatus(
                    userId,
                    mapOf("accountStatusId" to newStatusId)
                )
                if (response.isSuccessful) {
                    loadUsers()
                } else {
                    _error.value = "Błąd: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Błąd: ${e.message}"
            }
        }
    }
}
