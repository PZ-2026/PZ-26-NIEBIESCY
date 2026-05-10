package com.vectorpeaks.edulink.ui.screens.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vectorpeaks.edulink.data.model.RegisterRequest
import com.vectorpeaks.edulink.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val message: String) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

class RegisterViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        roleId: Int,
        city: String,
        phoneNumber: String,
        onSuccess: () -> Unit
    ) {
        // --- Walidacja pól wspólnych ---
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank() || city.isBlank()) {
            _uiState.value = RegisterUiState.Error("Wszystkie pola obowiązkowe (imię, nazwisko, email, hasło, miasto) muszą być wypełnione")
            return
        }
        if (password.length < 6) {
            _uiState.value = RegisterUiState.Error("Hasło musi mieć co najmniej 6 znaków")
            return
        }
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        if (!emailRegex.matches(email)) {
            _uiState.value = RegisterUiState.Error("Wprowadź poprawny adres e-mail")
            return
        }

        val isTutor = roleId == 2
        if (isTutor && phoneNumber.isBlank()) {
            _uiState.value = RegisterUiState.Error("Korepetytor musi podać numer telefonu")
            return
        }

        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            try {
                val request = RegisterRequest(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    password = password,
                    roleId = roleId,
                    city = city,
                    phoneNumber = phoneNumber
                )
                val response = RetrofitClient.apiService.register(request)

                if (response.isSuccessful) {
                    _uiState.value = RegisterUiState.Success("Rejestracja udana! Możesz się zalogować.")
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = when (response.code()) {
                        409 -> "Podany adres e-mail jest już zarejestrowany. Użyj innego lub zaloguj się."
                        else -> errorBody?.takeIf { it.isNotBlank() } ?: "Błąd rejestracji (kod ${response.code()})"
                    }
                    _uiState.value = RegisterUiState.Error(errorMessage)
                }
            } catch (e: HttpException) {
                val code = e.code()
                val message = if (code == 409) {
                    "Podany adres e-mail jest już zarejestrowany."
                } else {
                    "Błąd rejestracji. Sprawdź swoje dane lub spróbuj ponownie."
                }
                _uiState.value = RegisterUiState.Error(message)
            } catch (e: IOException) {
                _uiState.value = RegisterUiState.Error("Błąd sieci. Sprawdź połączenie z internetem.")
            } catch (e: Exception) {
                _uiState.value = RegisterUiState.Error("Wystąpił nieoczekiwany błąd. Spróbuj ponownie później.")
            }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}