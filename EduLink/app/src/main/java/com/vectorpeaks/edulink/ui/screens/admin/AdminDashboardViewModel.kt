package com.vectorpeaks.edulink.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vectorpeaks.edulink.data.model.Offer
import com.vectorpeaks.edulink.data.model.user.AdminStatsResponse
import com.vectorpeaks.edulink.data.model.user.BookingResponse
import com.vectorpeaks.edulink.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class AdminDashboardViewModel : ViewModel() {
    private val _stats = MutableStateFlow<AdminStatsResponse?>(null)
    val stats: StateFlow<AdminStatsResponse?> = _stats

    private val _pendingBookings = MutableStateFlow<List<BookingResponse>>(emptyList())
    val pendingBookings: StateFlow<List<BookingResponse>> = _pendingBookings

    private val _pendingOffers = MutableStateFlow<List<Offer>>(emptyList())
    val pendingOffers: StateFlow<List<Offer>> = _pendingOffers

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val statsResult = RetrofitClient.apiService.getAdminStats()
                _stats.value = statsResult
                val pendingResult = RetrofitClient.apiService.getPendingBookings()
                _pendingBookings.value = pendingResult
                val offersResult = RetrofitClient.apiService.getPendingOffers()
                _pendingOffers.value = offersResult
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

    /**
     * Approves a pending booking by updating its status to ACCEPTED.
     */
    fun approveBooking(bookingId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateBookingStatus(bookingId, "ACCEPTED")
                if (response.isSuccessful) {
                    loadDashboard()
                } else {
                    _error.value = "Błąd: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Błąd: ${e.message}"
            }
        }
    }

    /**
     * Rejects a pending booking by updating its status to REJECTED.
     */
    fun rejectBooking(bookingId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateBookingStatus(bookingId, "REJECTED")
                if (response.isSuccessful) {
                    loadDashboard()
                } else {
                    _error.value = "Błąd: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Błąd: ${e.message}"
            }
        }
    }

    /**
     * Approves a pending offer by updating its status to ACCEPTED.
     */
    fun approveOffer(offerId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateOfferStatus(offerId, "ACCEPTED")
                if (response.isSuccessful) {
                    loadDashboard()
                } else {
                    _error.value = "Błąd: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Błąd: ${e.message}"
            }
        }
    }

    /**
     * Rejects a pending offer by updating its status to REJECTED.
     */
    fun rejectOffer(offerId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateOfferStatus(offerId, "REJECTED")
                if (response.isSuccessful) {
                    loadDashboard()
                } else {
                    _error.value = "Błąd: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Błąd: ${e.message}"
            }
        }
    }
}
