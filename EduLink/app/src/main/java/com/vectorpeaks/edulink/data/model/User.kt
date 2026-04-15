package com.vectorpeaks.edulink.data.model

enum class RoleID {
    STUDENT, TUTOR, ADMIN
}

data class User(
    val id: Int,
    val role: RoleID,
    val password: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val accountStatusId: Int = 1,
    val address: String = "",
    val phone: String = "",
) {
    val fullName: String get() = "$firstName $lastName"

    val isBlocked: Boolean
        get() = accountStatusId == 0
}

