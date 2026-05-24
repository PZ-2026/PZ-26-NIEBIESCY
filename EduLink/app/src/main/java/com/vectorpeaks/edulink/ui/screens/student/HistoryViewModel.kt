package com.vectorpeaks.edulink.ui.screens.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vectorpeaks.edulink.data.model.user.BookingResponse
import com.vectorpeaks.edulink.data.model.user.ReviewRequest
import com.vectorpeaks.edulink.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class HistoryViewModel : ViewModel() {
    private val _bookings = MutableStateFlow<List<BookingResponse>>(emptyList())
    val bookings: StateFlow<List<BookingResponse>> = _bookings

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadBookings(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = RetrofitClient.apiService.getBookingsForStudent(studentId)
                _bookings.value = result
            } catch (e: HttpException) {
                _error.value = "HTTP error: ${e.code()}"
            } catch (e: IOException) {
                _error.value = "Network error"
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addReview(bookingId: Int, tutorId: Int, rating: Int, comment: String? = null, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val request = ReviewRequest(bookingId, tutorId, rating, comment)
                RetrofitClient.apiService.addReview(request)
                onSuccess()
            } catch (e: HttpException) {
                _error.value = "HTTP error: ${e.code()}"
            }
        }
    }

    fun completeBooking(bookingId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.completeBooking(bookingId)
                if (response.isSuccessful) onSuccess()
                else _error.value = "Błąd: ${response.code()}"
            } catch (e: HttpException) {
                _error.value = "HTTP error: ${e.code()}"
            } catch (e: Exception) {
                _error.value = "Błąd: ${e.message}"
            }
        }
    }
}