package com.vectorpeaks.edulink.data.model.user

data class ReviewResponse(
    val id: Int,
    val rating: Int,
    val comment: String?,
    val date: String,
    val subjectName: String? = null
)