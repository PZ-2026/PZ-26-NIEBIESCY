/*
 * ChatViewModelFactory.kt
 *
 * Version: 1.0
 * Date: 2026-05-10
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.edulink.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vectorpeaks.edulink.network.ApiService

/**
 * Factory for creating [ChatViewModel] instances with dependency injection.
 *
 * Since [ChatViewModel] requires an [ApiService] instance, we cannot use
 * the default ViewModelProvider constructor. This factory is called by
 * Compose when viewModel() is invoked with this factory.
 *
 * @param apiService the Retrofit API service to inject into ChatViewModel
 */
class ChatViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
