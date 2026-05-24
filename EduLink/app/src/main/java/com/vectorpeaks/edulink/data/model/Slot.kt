package com.vectorpeaks.edulink.data.model

import com.google.gson.annotations.SerializedName

data class Slot(
    val id: Int,
    val label: String,
    val dayOfWeek: Int = 0,
    @SerializedName("booked")
    val isBooked: Boolean = false
)