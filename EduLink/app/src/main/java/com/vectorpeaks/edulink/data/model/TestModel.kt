package com.vectorpeaks.edulink.data.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.vectorpeaks.edulink.data.remote.RetrofitClient

class TestModel : ViewModel() {

    val users = MutableLiveData<List<User>>()

    fun loadUsers() {
        viewModelScope.launch {
            try {
                val result = RetrofitClient.api.getUsers()
                users.postValue(result)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
