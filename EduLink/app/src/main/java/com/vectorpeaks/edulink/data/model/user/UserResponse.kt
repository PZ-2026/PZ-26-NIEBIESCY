package com.vectorpeaks.edulink.data.model.user

data class UserResponse(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String,
    val address: String,
    val phoneNumber: String
)