package com.vectorpeaks.edulink.data.model.user

data class AdminStatsResponse(
    val totalUsers: Int,
    val totalOffers: Int,
    val totalBookings: Int,
    val tutorsCount: Int,
    val studentsCount: Int,
    val pendingCount: Int
)
