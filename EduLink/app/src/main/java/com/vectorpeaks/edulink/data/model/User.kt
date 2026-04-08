package com.vectorpeaks.edulink.data.model

enum class UserRole {
    STUDENT, TUTOR, ADMIN
}

data class User(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val role: UserRole,
    val phone: String = "",
    val city: String = "",
    val avatarUrl: String = "",
    val isBlocked: Boolean = false
) {
    val fullName: String get() = "$firstName $lastName"
}
