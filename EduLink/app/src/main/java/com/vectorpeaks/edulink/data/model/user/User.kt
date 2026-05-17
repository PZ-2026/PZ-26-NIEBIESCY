package com.vectorpeaks.edulink.data.model.user

enum class RoleID {
    STUDENT, TUTOR, ADMIN
}

data class User(
    val id: Int,
    val roleId: Int,
    val password: String? = null,
    val firstName: String,
    val lastName: String,
    val email: String,
    val accountStatusId: Int = 1,
    val address: String = "",
    val phoneNumber: String = "",
) {
    val fullName: String get() = "$firstName $lastName"

    val isBlocked: Boolean
        get() = accountStatusId == 2

    fun getRole(): RoleID = when (roleId) {
        1 -> RoleID.ADMIN
        2 -> RoleID.TUTOR
        else -> RoleID.STUDENT
    }
}