package com.vectorpeaks.edulink.data.model.user

data class BookingResponse(
    val id: Int,
    val offerId: Int,
    val subject: String,
    val tutorName: String,
    val date: String,
    val time: String,
    val price: Double,
    val status: String,
    val rating: Int? = null,
    val tutorId: Int
)