package com.vectorpeaks.edulink.data.model

data class MaintenanceStatus(
    val active: Boolean = false,
    val startsAt: String? = null,
    val fullyActive: Boolean = false
)
