package com.vectorpeaks.edulink.data.model.user

data class AdminReportsResponse(
    val totalBookings: Int,
    val totalOffers: Int,
    val popularSubjects: List<SubjectEntry>
)

data class SubjectEntry(
    val name: String,
    val reviewCount: Int
)
