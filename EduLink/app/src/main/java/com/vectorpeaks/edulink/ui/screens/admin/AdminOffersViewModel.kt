package com.vectorpeaks.edulink.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vectorpeaks.edulink.data.model.Offer
import com.vectorpeaks.edulink.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import kotlinx.coroutines.async

class AdminOffersViewModel : ViewModel() {

    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: StateFlow<List<Offer>> = _offers

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadOffers() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _offers.value = RetrofitClient.apiService.getAllOffersForAdmin()
            } catch (e: HttpException) {
                _error.value = "HTTP error: ${e.code()}"
            } catch (e: IOException) {
                _error.value = "Network error"
            } catch (e: Exception) {
                _error.value = "Błąd: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun approveOffer(offerId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateOfferStatus(offerId, "ACCEPTED")
                if (response.isSuccessful) {
                    loadOffers()
                } else {
                    _error.value = "Błąd: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Błąd: ${e.message}"
            }
        }
    }

    fun rejectOffer(offerId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.updateOfferStatus(offerId, "REJECTED")
                if (response.isSuccessful) {
                    loadOffers()
                } else {
                    _error.value = "Błąd: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Błąd: ${e.message}"
            }
        }
    }
}