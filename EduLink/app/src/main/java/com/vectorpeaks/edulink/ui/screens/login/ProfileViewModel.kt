package com.vectorpeaks.edulink.ui.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
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

}