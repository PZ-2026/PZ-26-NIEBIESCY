package com.vectorpeaks.edulink.ui.screens.tutor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vectorpeaks.edulink.data.model.Offer
import com.vectorpeaks.edulink.data.model.OfferCreateRequest
import com.vectorpeaks.edulink.data.model.Slot
import com.vectorpeaks.edulink.data.model.SubjectDto
import com.vectorpeaks.edulink.network.RetrofitClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class TutorOffersViewModel : ViewModel() {

    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: StateFlow<List<Offer>> = _offers

    private val _slots = MutableStateFlow<List<Slot>>(emptyList())
    val slots: StateFlow<List<Slot>> = _slots

    private val _subjects = MutableStateFlow<List<SubjectDto>>(emptyList())
    val subjects: StateFlow<List<SubjectDto>> = _subjects

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating

    fun loadData(tutorId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val offersDeferred = async { RetrofitClient.apiService.getOffersByTutor(tutorId) }
                val slotsDeferred = async { RetrofitClient.apiService.getAvailabilitySlots() }
                val subjectsDeferred = async { RetrofitClient.apiService.getSubjectsWithId() }

                _offers.value = offersDeferred.await()
                _slots.value = slotsDeferred.await()
                _subjects.value = subjectsDeferred.await()
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

    fun createOffer(request: OfferCreateRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isCreating.value = true
            try {
                val response = RetrofitClient.apiService.createOffer(request)
                if (response.isSuccessful) {
                    loadData(request.tutorId)
                    onSuccess()
                } else {
                    _error.value = "Failed to create offer: ${response.code()}"
                }
            } catch (e: HttpException) {
                _error.value = "HTTP error: ${e.code()}"
            } catch (e: IOException) {
                _error.value = "Network error"
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isCreating.value = false
            }
        }
    }
}