package com.vectorpeaks.edulink.data.model

data class Offer(
    val id: Int,
    val tutorId: Int,
    val tutorName: String,
    val subject: String,
    val description: String,
    val pricePerHour: Double,
    val city: String,
    val isOnline: Boolean = false,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val isApproved: Boolean = true,
    val availableSlots: List<Slot>
)