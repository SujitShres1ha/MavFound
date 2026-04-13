package com.example.mavfound.models

data class Report(
    val reportId: Int = 0,
    val reporterId: Int,
    val targetUserId: Int? = null,
    val listingId: Int? = null,
    val reason: String,
    val status: String = "Pending"
)