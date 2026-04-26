package com.vectorpeaks.edulink.ui.screens.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vectorpeaks.edulink.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DataViewModel : ViewModel() {
    private val _subjects = MutableStateFlow<List<String>>(emptyList())
    val subjects: StateFlow<List<String>> = _subjects

    private val _cities = MutableStateFlow<List<String>>(emptyList())
    val cities: StateFlow<List<String>> = _cities

    fun loadSubjects() {
        viewModelScope.launch {
            try {
                val result = RetrofitClient.apiService.getSubjects()
                _subjects.value = result
            } catch (e: Exception) {

            }
        }
    }

    fun loadCities() {
        viewModelScope.launch {
            try {
                val result = RetrofitClient.apiService.getCities()
                _cities.value = result
            } catch (e: Exception) {

            }
        }
    }
}