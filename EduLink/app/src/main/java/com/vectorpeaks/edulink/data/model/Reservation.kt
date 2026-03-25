package com.vectorpeaks.edulink.data.model

enum class ReservationStatus {
    PENDING, ACCEPTED, REJECTED, COMPLETED
}

data class Reservation(
    val id: Int,
    val offerId: Int,
    val studentId: Int,
    val studentName: String,
    val tutorId: Int,
    val tutorName: String,
    val subject: String,
    val date: String,
    val time: String,
    val price: Double,
    val status: ReservationStatus,
    val rating: Int? = null
)
