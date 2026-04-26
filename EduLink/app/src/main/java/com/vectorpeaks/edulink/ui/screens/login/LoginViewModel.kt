// LoginViewModel.kt
package com.vectorpeaks.edulink.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vectorpeaks.edulink.data.model.LoginRequest
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.data.model.user.UserResponse
import com.vectorpeaks.edulink.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Proszę wypełnić wszystkie pola")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val request = LoginRequest(email.trim(), password)
                val response: UserResponse = RetrofitClient.apiService.login(request)

                // Mapowanie stringa role na Int (zgodnie z bazą: 1-ADMIN, 2-TUTOR, 3-STUDENT)
                val roleId = when (response.role.uppercase()) {
                    "ADMIN" -> 1
                    "TUTOR" -> 2
                    else -> 3
                }

                val user = User(
                    id = response.id,
                    roleId = roleId,
                    password = null,
                    firstName = response.firstName,
                    lastName = response.lastName,
                    email = response.email,
                    accountStatusId = 1,
                    address = response.address ?: "",
                    phoneNumber = response.phoneNumber ?: ""
                )
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

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}