package com.vectorpeaks.edulink.data.model

data class UserResponse(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String
)