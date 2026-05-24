package com.vectorpeaks.edulink.data.model.user

data class ReviewRequest(
    val bookingId: Int,
    val tutorId: Int,
    val rating: Int,
    val comment: String? = null
)