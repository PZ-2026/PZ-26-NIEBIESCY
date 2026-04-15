package com.vectorpeaks.edulink.network

import com.vectorpeaks.edulink.data.model.LoginRequest
import com.vectorpeaks.edulink.data.model.UserResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): UserResponse
}