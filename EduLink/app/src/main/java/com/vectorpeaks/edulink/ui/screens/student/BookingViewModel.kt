package com.vectorpeaks.edulink.ui.screens.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vectorpeaks.edulink.data.model.BookingRequest
import com.vectorpeaks.edulink.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed class BookingUiState {
    object Idle : BookingUiState()
    object Loading : BookingUiState()
    data class Success(val message: String) : BookingUiState()
    data class Error(val message: String) : BookingUiState()
}

class BookingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<BookingUiState>(BookingUiState.Idle)
    val uiState: StateFlow<BookingUiState> = _uiState

    fun createBooking(offerId: Int, studentId: Int, slotId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = BookingUiState.Loading
            try {
                val request = BookingRequest(offerId, studentId, slotId)
                val response = RetrofitClient.apiService.createBooking(request)
                if (response.isSuccessful) {
                    _uiState.value = BookingUiState.Success("Rezerwacja została złożona")
                    onSuccess()
                } else {
                    _uiState.value = BookingUiState.Error("Błąd: ${response.code()}")
                }
            } catch (e: HttpException) {
                _uiState.value = BookingUiState.Error("HTTP error: ${e.code()}")
            } catch (e: Exception) {
                _uiState.value = BookingUiState.Error("Błąd: ${e.message}")
            }
        }
    }
}