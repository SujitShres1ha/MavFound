package com.example.mavfound.data.model

data class ClaimRecord(
    val id: String,
    val listingId: String,
    val claimantUserId: String,
    val paymentId: String,
    val exchangeCode: String,
    val status: ListingStatus,
    val createdAtMillis: Long,
    val completedAtMillis: Long? = null
)
