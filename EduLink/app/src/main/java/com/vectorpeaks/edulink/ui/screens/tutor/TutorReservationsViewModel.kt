package com.vectorpeaks.edulink.ui.screens.tutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vectorpeaks.edulink.data.model.user.BookingResponse
import com.vectorpeaks.edulink.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class TutorReservationsViewModel : ViewModel() {
    private val _bookings = MutableStateFlow<List<BookingResponse>>(emptyList())
    val bookings: StateFlow<List<BookingResponse>> = _bookings

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadBookings(tutorId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = RetrofitClient.apiService.getBookingsForTutor(tutorId)
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

    fun updateStatus(bookingId: Int, status: String, tutorId: Int) {
        viewModelScope.launch {
            try {
                RetrofitClient.apiService.updateBookingStatus(bookingId, status)
                loadBookings(tutorId)
            } catch (e: HttpException) {
                _error.value = "HTTP error: ${e.code()}"
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }

    fun completeBooking(bookingId: Int, tutorId: Int) {
        updateStatus(bookingId, "COMPLETED", tutorId)
    }
}