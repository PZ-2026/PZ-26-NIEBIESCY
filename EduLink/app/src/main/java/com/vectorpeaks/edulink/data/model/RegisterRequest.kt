package com.vectorpeaks.edulink.data.model

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val roleId: Int,
    val city: String,
    val phoneNumber: String
)