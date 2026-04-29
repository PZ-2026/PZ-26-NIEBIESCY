package com.vectorpeaks.edulink.data.model

data class BookingRequest(
    val offerId: Int,
    val studentId: Int,
    val availabilitySlotId: Int
)