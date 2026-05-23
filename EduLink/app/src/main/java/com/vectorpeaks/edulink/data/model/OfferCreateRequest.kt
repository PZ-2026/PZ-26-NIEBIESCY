package com.vectorpeaks.edulink.data.model

data class OfferCreateRequest(
    val tutorId: Int,
    val subjectId: Int,
    val details: String,
    val price: Double,
    val offerType: String,
    val availabilitySlotIds: List<Int> = emptyList()
)